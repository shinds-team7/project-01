package com.example.petnow.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationType;
import com.example.petnow.entity.ReservationUseStatus;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReservationErrorCode;
import com.example.petnow.mapper.PetMapper;

import com.example.petnow.mapper.PlaceAvailabilityMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;
	private final PlaceMapper placeMapper;
	private final PetMapper petMapper;
	private final PlaceAvailabilityMapper placeAvailabilityMapper;

	private static final long MIN_RESERVATION_MINUTES = PlaceAvailabilityServiceImpl.SLOT_HOURS * 60L;

	@Override
	@Transactional
	public String saveReservation(ReservationRequest request, Long userId) {
		Place place = placeMapper.findById(request.getPlaceId());
		if (place == null) {
			throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
		}

		ReservationType reservationType = request.getReservationType();
		validateTypeSupported(place, reservationType);
		validatePeriod(request.getCheckIn(), request.getCheckOut());
		validateTypeRule(place, reservationType, request.getCheckIn(), request.getCheckOut());
		validateOwnPets(userId, request.getPetIds());

		occupySlots(request.getPlaceId(), request.getCheckIn(), request.getCheckOut());

		String reservationNo = Reservation.createReservationNo();
		BigDecimal totalPrice = calculateTotalPrice(place, reservationType, request.getCheckIn(), request.getCheckOut());

		Reservation reservation = Reservation.builder()
			.placeId(request.getPlaceId())
			.userId(userId)
			.memo(request.getMemo())
			.totalPrice(totalPrice)
			.reservationType(reservationType)
			.checkIn(request.getCheckIn())
			.checkOut(request.getCheckOut())
			.status(ReservationStatus.PENDING)
			.reservationNo(reservationNo)
			.build();

		reservationMapper.save(reservation);

		List<Long> slotIds = placeAvailabilityMapper.findSlotIdsByRange(
			request.getPlaceId(), request.getCheckIn(), request.getCheckOut());
		reservationMapper.insertReservationSlots(reservation.getId(), slotIds);

		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservationNo;
	}

	/**
	 * 요청 구간의 OPEN 슬롯을 한 번의 UPDATE 로 RESERVED 로 바꾼다.
	 *
	 * 변경된 행 수가 필요한 슬롯 개수와 다르면 예약할 수 없는 시간이 섞여 있다는 뜻이므로
	 * 예외를 던져 트랜잭션 전체를 롤백한다. 이 검사를 빠뜨리면 일부만 점유된 채로 예약이 만들어진다.
	 */
	private void occupySlots(Long placeId, LocalDateTime checkIn, LocalDateTime checkOut) {
		long hours = Duration.between(checkIn, checkOut).toHours();
		if (hours % PlaceAvailabilityServiceImpl.SLOT_HOURS != 0) {
			throw new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE);
		}

		int requiredSlots = (int) (hours / PlaceAvailabilityServiceImpl.SLOT_HOURS);
		int reservedSlots = placeAvailabilityMapper.updateSlotsToReserved(placeId, checkIn, checkOut);

		if (reservedSlots != requiredSlots) {
			throw new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE);
		}
	}

	/**
	 * 점유했던 슬롯을 다시 OPEN 으로 되돌린다.
	 * 취소와 거절 양쪽에서 호출해야 한다. 빠뜨리면 그 시간이 영영 막힌다.
	 */
	private void releaseSlots(Long reservationId) {
		List<Long> slotIds = reservationMapper.findSlotIdsByReservationId(reservationId);
		if (slotIds.isEmpty()) {
			return;
		}
		placeAvailabilityMapper.updateSlotsToOpen(slotIds);
		reservationMapper.deleteReservationSlotsByReservationId(reservationId);
	}

	private void validateTypeSupported(Place place, ReservationType reservationType) {
		boolean supported = (reservationType == ReservationType.SAME_DAY && place.isSupportsHourly())
			|| (reservationType == ReservationType.OVERNIGHT && place.isSupportsPackage());
		if (!supported) {
			throw new BusinessException(ReservationErrorCode.UNSUPPORTED_RESERVATION_TYPE);
		}
	}

	private void validatePeriod(LocalDateTime checkIn, LocalDateTime checkOut) {
		if (!checkOut.isAfter(checkIn)) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
		}
		if (Duration.between(checkIn, checkOut).toMinutes() < MIN_RESERVATION_MINUTES) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_TOO_SHORT);
		}
	}

	private void validateTypeRule(Place place, ReservationType reservationType,
		LocalDateTime checkIn, LocalDateTime checkOut) {

		if (reservationType == ReservationType.SAME_DAY) {
			/* 24시 종료는 다음 날 00시로 들어오므로 그 경우만 예외로 허용한다 */
			boolean sameDate = checkIn.toLocalDate().equals(checkOut.toLocalDate());
			boolean endsAtMidnight = checkOut.toLocalTime().equals(LocalTime.MIDNIGHT)
				&& checkOut.toLocalDate().equals(checkIn.toLocalDate().plusDays(1));
			if (!sameDate && !endsAtMidnight) {
				throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
			}
			return;
		}

		LocalTime checkInTime = PlaceAvailabilityServiceImpl.resolveCheckInTime(place);
		LocalTime checkOutTime = PlaceAvailabilityServiceImpl.resolveCheckOutTime(place);

		/*
		 * 격자를 벗어난 입/퇴실 시각이 저장되면 필요 슬롯 개수가 정수로 떨어지지 않아
		 * 그 place 의 패키지 예약이 항상 거절된다. 원인이 요청이 아니라 장소 설정에 있으므로
		 * 슬롯 부족이 아니라 시각 오류로 구분해 알린다.
		 */
		if (!PlaceAvailabilityServiceImpl.isOnGrid(checkInTime)
			|| !PlaceAvailabilityServiceImpl.isOnGrid(checkOutTime)) {
			throw new BusinessException(ReservationErrorCode.INVALID_PACKAGE_TIME);
		}

		if (!checkIn.toLocalTime().equals(checkInTime) || !checkOut.toLocalTime().equals(checkOutTime)) {
			throw new BusinessException(ReservationErrorCode.INVALID_PACKAGE_TIME);
		}
		if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
		}
	}

	private void validateOwnPets(Long userId, List<Long> petIds) {
		List<PetListResponse> myPets = petMapper.getPetList(userId);
		Set<Long> myPetIds = myPets.stream()
			.map(PetListResponse::getId)
			.collect(Collectors.toSet());
		if (!myPetIds.containsAll(petIds)) {
			throw new BusinessException(ReservationErrorCode.PET_NOT_FOUND);
		}
	}

	@Override
	public List<ReservationListResponse> getReservationList(Long userId, String useStatus) {
		ReservationUseStatus status = parseUseStatus(useStatus);

		boolean beforeUse = (status == ReservationUseStatus.BEFORE_USE);
		boolean inUse = (status == ReservationUseStatus.IN_USE);
		boolean afterUse = (status == ReservationUseStatus.AFTER_USE);

		return reservationMapper.viewReservationList(userId, beforeUse, inUse, afterUse);
	}

	@Override
	public ReservationDetailResponse detailReservation(Long reservationId, Long userId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null || !reservation.getUserId().equals(userId)) {
			throw new BusinessException(AuthErrorCode.FORBIDDEN);
		}

		return reservationMapper.detailReservation(reservationId);
	}

	@Override
	@Transactional
	public void cancelReservation(Long reservationId, Long userId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		if (!reservation.getUserId().equals(userId)) {
			throw new BusinessException(AuthErrorCode.FORBIDDEN);
		}

		int updatedRows = reservationMapper.cancelReservation(reservationId);
		if (updatedRows == 0) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}

		releaseSlots(reservationId);
	}

	@Override
	@Transactional
	public void approveReservation(Long reservationId, Long hostUserId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		PlaceDetailResponse place = placeMapper.findDetailById(reservation.getPlaceId());
		if (place == null || !place.getHostUserId().equals(hostUserId)) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_ACCESS_DENIED);
		}

		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
		}

		int result = reservationMapper.approveReservation(reservationId);
		if (result != 1) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}
	}

	@Override
	@Transactional
	public void rejectReservation(Long reservationId, Long hostUserId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		PlaceDetailResponse place = placeMapper.findDetailById(reservation.getPlaceId());
		if (place == null || !place.getHostUserId().equals(hostUserId)) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_ACCESS_DENIED);
		}

		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
		}

		int result = reservationMapper.rejectReservation(reservationId);
		if (result != 1) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}

		releaseSlots(reservationId);
	}

	@Override
	public List<ReservationListResponse> getReservationByHost(Long hostUserId, ReservationStatus status) {
		return reservationMapper.viewReservationListByHost(hostUserId, status);
	}

	private ReservationUseStatus parseUseStatus(String useStatus) {
		if (useStatus == null || useStatus.trim().isEmpty()) {
			return null;
		}
		try {
			return ReservationUseStatus.valueOf(useStatus.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private BigDecimal calculateTotalPrice(Place place, ReservationType reservationType, LocalDateTime checkIn, LocalDateTime checkOut) {
		BigDecimal totalPrice;
		if (reservationType == ReservationType.SAME_DAY) {
			BigDecimal hourlyPrice = place.getHourlyPrice();
			if (hourlyPrice == null) {
				throw new BusinessException(ReservationErrorCode.HOURLY_PRICE_NOT_SET);
			}
			long totalHours = Duration.between(checkIn, checkOut).toHours();
			totalPrice = hourlyPrice.multiply(BigDecimal.valueOf(totalHours));
		} else {
			BigDecimal nightlyPrice = place.getNightlyPrice();
			if (nightlyPrice == null) {
				throw new BusinessException(ReservationErrorCode.NIGHTLY_PRICE_NOT_SET);
			}
			long totalDays = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
			totalPrice = nightlyPrice.multiply(BigDecimal.valueOf(totalDays));
		}
		return totalPrice;
	}
}
