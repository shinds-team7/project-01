package com.example.petnow.entity;

import java.time.LocalDateTime;
import java.util.List;

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
public class Reservation {
	private Long id;
	private ReservationStatus status;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String memo;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private User user;
	private Place place;
	private List<Pet> pets;
}
