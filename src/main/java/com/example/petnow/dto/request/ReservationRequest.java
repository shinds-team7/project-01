package com.example.petnow.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.example.petnow.entity.ReservationType;

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

	/**
	 * 사용자가 화면에서 먼저 고른 예약 유형.
	 * 날짜로 유형을 추측하지 않고 이 값을 신뢰한 뒤, 날짜와 모순되지 않는지만 검증한다.
	 */
	@NotNull
	private ReservationType reservationType;

	@NotEmpty
	private List<Long> petIds;

	private String memo;

	@NotNull
	private LocalDateTime checkIn;

	@NotNull
	private LocalDateTime checkOut;

}
