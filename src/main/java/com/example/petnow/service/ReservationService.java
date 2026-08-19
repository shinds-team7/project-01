package com.example.petnow.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationType;
import com.example.petnow.dto.response.ReservationDetailResponse;

public interface ReservationService {
	String saveReservation(ReservationRequest request, Long userId);

	ReservationDetailResponse detailReservation(Long reservationId, Long userId);

	List<ReservationListResponse> getReservationList(Long userId, String useStatus);

	void cancelReservation(Long reservationId, Long userId);

	void approveReservation(Long reservationId, Long hostUserId);

	void rejectReservation(Long reservationId, Long hostUserId);

	List<ReservationListResponse> getReservationByHost(Long loginUserId, ReservationStatus status);

    ReservationStepResponse resolveHourly(Long placeId, String date, Long start, Long end);

    ReservationStepResponse resolveConfirm(Long placeId, ReservationType reservationType, LocalDateTime checkIn, LocalDateTime checkOut);

    ReservationStepResponse resolvePackage(Long placeId, String startDate, String endDate);

    void changeReservationPet(Long reservationId, List<Long> petIds, Long userId);
}
