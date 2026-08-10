package com.example.petnow.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ReservationRequest {

	@NotNull
	private Long placeId;

	@NotEmpty
	private List<Long> petIds;

	private String memo;

	@NotNull
	private LocalDateTime checkIn;

	@NotNull
	private LocalDateTime checkOut;

}
