package com.example.petnow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.petnow.entity.Reservation;

@Mapper
public interface ReservationMapper {
	void save(Reservation reservation);

	void saveReservationPets(@Param("reservationId") Long reservationId, @Param("petIds") List<Long> petIds);

}
