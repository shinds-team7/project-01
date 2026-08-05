package com.example.petnow.entity;

import java.math.BigDecimal;
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
	private Long userId;
	private Long placeId;
	private String reservationType;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private ReservationStatus status;
	private String memo;
	private BigDecimal totalPrice;
	private LocalDateTime respondedAt;
	private LocalDateTime canceledAt;
	private String cancelReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private List<Pet> pets;
}
