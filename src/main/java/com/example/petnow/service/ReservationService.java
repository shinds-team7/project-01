package com.example.petnow.service;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.ReservationDetailResponse;

public interface ReservationService {
	String saveReservation(ReservationRequest request, Long userId);

	ReservationDetailResponse detailReservation(Long reservationId);

}
