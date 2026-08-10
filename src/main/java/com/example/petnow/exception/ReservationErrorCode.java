package com.example.petnow.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

	INVALID_RESERVATION_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_PERIOD", "체크인/체크아웃 날짜가 올바르지 않습니다."),
	INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_STATUS", "처리할 수 없는 예약 상태입니다."),
	RESERVATION_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "RESERVATION_UPDATE_FAILED", "상태 업데이트에 실패했습니다."),
	RESERVATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "RESERVATION_TOO_SHORT", "예약 시간은 최소 1시간 이상입니다."),
	PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND","해당 반려동물을 찾을 수 없습니다."),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "해당 예약을 찾을 수 없습니다."),
	HOURLY_PRICE_NOT_SET(HttpStatus.BAD_REQUEST, "HOURLY_PRICE_NOT_SET", "시간당 가격이 설정되어 있지 않습니다."),
	NIGHTLY_PRICE_NOT_SET(HttpStatus.BAD_REQUEST, "NIGHTLY_PRICE_NOT_SET", "1박당 가격이 설정되어 있지 않습니다."),
	RESERVATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RESERVATION_ACCESS_DENIED", "예약에 접근할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
