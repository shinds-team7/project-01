package com.example.petnow.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

	private Long placeId;
	private List<Long> petIds;
	private String memo;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;

}
