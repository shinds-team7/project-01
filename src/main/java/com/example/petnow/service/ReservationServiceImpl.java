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
import com.example.petnow.entity.ReservationType;
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

		ReservationType reservationType = determineReservationType(request.getCheckIn(), request.getCheckOut());
		BigDecimal totalPrice = calculateTotalPrice(place, reservationType, request.getCheckIn(), request.getCheckOut());

		Reservation reservation = Reservation.builder()
			.placeId(request.getPlaceId())
			.userId(userId)
			.memo(request.getMemo())
			.totalPrice(totalPrice)
			.reservationType(reservationType.getDatabaseValue())
			.checkIn(request.getCheckIn())
			.checkOut(request.getCheckOut())
			.status(ReservationStatus.PENDING)
			.build();

		reservationMapper.save(reservation);
		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservation.getId();
	}

	private ReservationType determineReservationType(LocalDateTime checkIn, LocalDateTime checkOut) {
		if (checkIn.toLocalDate().equals(checkOut.toLocalDate())) {
			return ReservationType.DAY_USE;
		} else {
			return ReservationType.OVERNIGHT;
		}
	}

	private BigDecimal calculateTotalPrice(Place place, ReservationType reservationType,
		LocalDateTime checkIn, LocalDateTime checkOut) {
		return switch (reservationType) {
			case DAY_USE -> {
				long totalHours = Duration.between(checkIn, checkOut).toHours();
				yield place.getHourlyPrice().multiply(BigDecimal.valueOf(totalHours));
			}
			case OVERNIGHT -> {
				long totalDays = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
				yield place.getNightlyPrice().multiply(BigDecimal.valueOf(totalDays));
			}
		};
	}

}
