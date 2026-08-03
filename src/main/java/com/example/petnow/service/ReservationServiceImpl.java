package com.example.petnow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;

	@Override
	@Transactional
	public Long save(ReservationRequest request, Long userId) {
		Reservation reservation = Reservation.builder()
			.placeId(request.getPlaceId())
			.userId(userId)
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
