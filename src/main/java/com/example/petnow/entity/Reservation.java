package com.example.petnow.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.example.petnow.common.domain.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends BaseEntity {
	private Long id;
	private Long userId;
	private Long placeId;
	private ReservationStatus status;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String memo;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private List<Pet> pets;
}
