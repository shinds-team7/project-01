package com.example.petnow.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
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
	public Long saveReservation(ReservationRequest request, Long userId) {
		Place place = placeMapper.findById(request.getPlaceId());

		String reservationType = determineReservationType(request.getCheckIn(), request.getCheckOut());
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
			.build();

		reservationMapper.save(reservation);
		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservation.getId();
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
