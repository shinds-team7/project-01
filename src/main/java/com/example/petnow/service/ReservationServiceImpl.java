package com.example.petnow.service;

import org.springframework.stereotype.Service;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.User;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;

	@Override
	public Long save(ReservationRequest request, Long userId) {
		Reservation reservation = Reservation.builder()
			.place(Place.builder().id(request.getPlaceId()).build())
			.user(User.builder().id(userId).build())
			.memo(request.getMemo())
			.checkIn(request.getCheckIn())
			.checkOut(request.getCheckOut())
			.status(ReservationStatus.PENDING)
			.build();

		reservationMapper.save(reservation);
		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservation.getId();
	}
}
