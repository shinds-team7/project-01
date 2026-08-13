package com.example.petnow.service;

import java.util.List;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationUseStatus;
import com.example.petnow.dto.response.ReservationDetailResponse;

public interface ReservationService {
	String saveReservation(ReservationRequest request, Long userId);

	ReservationDetailResponse detailReservation(Long reservationId, Long userId);

	List<ReservationListResponse> getReservationList(Long userId, String useStatus);

	void cancelReservation(Long reservationId, Long userId);

	void approveReservation(Long reservationId, Long hostUserId);

	void rejectReservation(Long reservationId, Long hostUserId);

	List<ReservationListResponse> getReservationByHost(Long loginUserId, ReservationStatus status);
}
