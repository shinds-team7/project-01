package com.example.petnow.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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

import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;
	private final PlaceMapper placeMapper;
	private final PetMapper petMapper;

	private static final long MIN_RESERVATION_MINUTES = 60;

	@Override
	@Transactional
	public String saveReservation(ReservationRequest request, Long userId) {
		Place place = placeMapper.findById(request.getPlaceId());
		if (place == null ) {
			throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
		}

		if (request.getCheckOut().isBefore(request.getCheckIn())
			|| request.getCheckOut().isEqual(request.getCheckIn())) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
		}

		if (Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes() < MIN_RESERVATION_MINUTES) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_TOO_SHORT);
		}

		List<PetListResponse> myPets = petMapper.getPetList(userId);
		Set<Long> myPetIds = myPets.stream()
			.map(PetListResponse::getId)
			.collect(Collectors.toSet());
		if (!myPetIds.containsAll(request.getPetIds())) {
			throw new BusinessException(ReservationErrorCode.PET_NOT_FOUND);
		}


		String reservationNo = Reservation.createReservationNo();
		BigDecimal totalPrice = calculateTotalPrice(place, request.getReservationType(), request.getCheckIn(), request.getCheckOut());

		Reservation reservation = Reservation.builder()
			.placeId(request.getPlaceId())
			.userId(userId)
			.memo(request.getMemo())
			.totalPrice(totalPrice)
			.reservationType(request.getReservationType())
			.checkIn(request.getCheckIn())
			.checkOut(request.getCheckOut())
			.status(ReservationStatus.PENDING)
			.reservationNo(reservationNo)
			.build();

		reservationMapper.save(reservation);
		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservationNo;
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
	}

	@Override
	public List<ReservationListResponse> getReservationByHost(Long loginUserId, ReservationStatus status) {
		return reservationMapper.viewReservationListByHost(loginUserId, status);
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
			totalPrice = place.getHourlyPrice().multiply(BigDecimal.valueOf(totalHours));
		} else {
			BigDecimal nightlyPrice = place.getNightlyPrice();
			if (nightlyPrice == null) {
				throw new BusinessException(ReservationErrorCode.NIGHTLY_PRICE_NOT_SET);
			}
			long totalDays = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
			totalPrice = place.getNightlyPrice().multiply(BigDecimal.valueOf(totalDays));
		}
		return totalPrice;
	}
}
