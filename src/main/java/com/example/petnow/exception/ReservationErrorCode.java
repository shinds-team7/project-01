package com.example.petnow.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

	INVALID_RESERVATION_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_RESERVATIO_PERIOD", "체크인/체크아웃 날짜가 올바르지 않습니다."),
	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND","해당 반려동물을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
