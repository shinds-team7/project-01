package com.example.petnow.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationType {
	SAME_DAY("당일"), OVERNIGHT("숙박");

	private final String label;

	public static ReservationType fromLabel(String label) {
		if (SAME_DAY.getLabel().equals(label)) {
			return SAME_DAY;
		} else if (OVERNIGHT.getLabel().equals(label)) {
			return OVERNIGHT;
		} else {
			throw new IllegalArgumentException("알 수 없는 예약 유형: " + label);
		}
	}
}
