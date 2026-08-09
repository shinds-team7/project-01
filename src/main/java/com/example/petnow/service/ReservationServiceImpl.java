package com.example.petnow.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationUseStatus;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;
	private final PlaceMapper placeMapper;

	@Override
	@Transactional
	public String saveReservation(ReservationRequest request, Long userId) {
		Place place = placeMapper.findById(request.getPlaceId());

		String reservationType = determineReservationType(request.getCheckIn(), request.getCheckOut());
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
	public ReservationDetailResponse detailReservation(Long reservationId) {
		ReservationDetailResponse reservation = reservationMapper.detailReservation(reservationId);
		if (reservation == null) {
			throw new IllegalStateException("해당 예약이 없습니다.");
		}

		return reservation;
	}

	@Override
	public void cancelReservation(Long reservationId, Long userId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (!reservation.getUserId().equals(userId)) {
			throw new IllegalStateException("사용자가 일치하지 않습니다.");
		}

		if (reservation.getStatus() == ReservationStatus.CANCELED) {
			throw new IllegalStateException("이미 취소된 예약입니다.");
		}

		reservationMapper.cancelReservation(reservationId);
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

	private String determineReservationType(LocalDateTime checkIn, LocalDateTime checkOut) {
		if (checkIn.toLocalDate().equals(checkOut.toLocalDate())) {
			return "당일";
		} else {
			return "숙박";
		}
	}

	private BigDecimal calculateTotalPrice(Place place, String reservationType, LocalDateTime checkIn, LocalDateTime checkOut) {
		BigDecimal totalPrice;
		if ("당일".equals(reservationType)) {
			long totalHours = Duration.between(checkIn, checkOut).toHours();
			totalPrice = place.getHourlyPrice().multiply(BigDecimal.valueOf(totalHours));
		} else {
			long totalDays = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
			totalPrice = place.getNightlyPrice().multiply(BigDecimal.valueOf(totalDays));
		}
		return totalPrice;
	}

}
