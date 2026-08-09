package com.example.petnow.service;

import java.util.List;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.ReservationUseStatus;
import com.example.petnow.dto.response.ReservationDetailResponse;

public interface ReservationService {
	String saveReservation(ReservationRequest request, Long userId);

	ReservationDetailResponse detailReservation(Long reservationId);

	List<ReservationListResponse> getReservationList(Long userId, String useStatus);

	void cancelReservation(Long reservationId, Long userId);
}
