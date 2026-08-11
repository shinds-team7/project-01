package com.example.petnow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;

@Mapper
public interface ReservationMapper {
	void save(Reservation reservation);

	void saveReservationPets(@Param("reservationId") Long reservationId, @Param("petIds") List<Long> petIds);

	List<ReservationListResponse> viewReservationList(
		@Param("userId") Long userId,
		@Param("beforeUse") Boolean beforeUse,
		@Param("inUse") Boolean inUse,
		@Param("afterUse") Boolean afterUse
	);

	List<ReservationListResponse> viewReservationListByHost(@Param("hostUserId") Long hostUserId,
		@Param("status")ReservationStatus status);

	ReservationDetailResponse detailReservation(Long reservationId);

	Reservation findById(Long reservationId);

	int cancelReservation(@Param("reservationId") Long reservationId);

	int approveReservation(@Param("reservationId") Long reservationId);

	int rejectReservation(@Param("reservationId") Long reservationId);
}
