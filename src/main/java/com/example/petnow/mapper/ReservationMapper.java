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

	List<ReservationListResponse> viewReservationListByHost(@Param("loginUserId") Long loginUserId,
		@Param("status")ReservationStatus status);

	ReservationDetailResponse detailReservation(Long reservationId);

    ReservationDetailResponse detailReservationForHost(@Param("reservationId") Long reservationId, @Param("hostUserId") Long hostUserId);

	Reservation findById(Long reservationId);

	int cancelReservation(Long reservationId);

	int approveReservation(Long reservationId);

	int rejectReservation(Long reservationId);

	void insertReservationSlots(@Param("reservationId") Long reservationId, @Param("slotIds") List<Long> slotIds);

    List<Long> findSlotIdsByReservationId(Long reservationId);

    int deleteReservationSlotsByReservationId(Long reservationId);

    int deleteReservationPets(Long reservationId);
}
