package com.example.petnow.entity;

import java.time.LocalDateTime;

public enum ReservationUseStatus {
	BEFORE_USE, IN_USE, AFTER_USE;

	// 예약 승인/거부 구현 후 수정 예정. 임시 구현
	public static ReservationUseStatus calculate(ReservationStatus useStatus, LocalDateTime checkIn, LocalDateTime checkOut) {
		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(checkIn)) {
			return BEFORE_USE;
		} else if (now.isAfter(checkIn) && now.isBefore(checkOut)) {
			return IN_USE;
		} else {
			return AFTER_USE;
		}
	}

	public boolean getIsBeforeUse() {
		return this == BEFORE_USE;
	}

	public boolean getIsInUse() {
		return this == IN_USE;
	}

	public boolean getIsAfterUse() {
		return this == AFTER_USE;
	}
}
