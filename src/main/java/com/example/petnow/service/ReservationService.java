package com.example.petnow.service;

import com.example.petnow.dto.request.ReservationRequest;

public interface ReservationService {
	Long saveReservation(ReservationRequest request, Long userId);

}
