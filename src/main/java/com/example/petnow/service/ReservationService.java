package com.example.petnow.service;

import com.example.petnow.dto.request.ReservationRequest;

public interface ReservationService {
	Long save(ReservationRequest request, Long userId);

}
