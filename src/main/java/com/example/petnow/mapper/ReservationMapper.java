package com.example.petnow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.entity.Reservation;


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
	ReservationDetailResponse detailReservation(Long reservationId);

	Reservation findById(Long reservationId);

	void cancelReservation(Long reservationId);
}
