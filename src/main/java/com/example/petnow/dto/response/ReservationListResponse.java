package com.example.petnow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationUseStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationListResponse {
	private Long reservationId;
	private Long placeId;
	private String placeName;
	private BigDecimal totalPrice;
	private ReservationStatus reservationStatus;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;

	// 예약 승인/거부 구현 후 수정 예정. 임시 구현
	public ReservationUseStatus getReservationUseStatus() {
		if (this.reservationStatus != ReservationStatus.CONFIRMED) {
			return null;
		}

		if (checkIn == null || checkOut == null) return null;

		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(checkIn)) {
			return ReservationUseStatus.BEFORE_USE;
		} else if (now.isAfter(checkOut)) {
			return ReservationUseStatus.AFTER_USE;
		} else {
			return ReservationUseStatus.IN_USE;
		}
	}

}
