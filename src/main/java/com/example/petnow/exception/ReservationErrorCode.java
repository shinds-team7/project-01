package com.example.petnow.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    SLOT_ALREADY_TAKEN(HttpStatus.CONFLICT, "SLOT_ALREADY_TAKEN", "다른 사용자가 먼저 예약한 시간이 있습니다. 다시 선택해주세요."),
	SLOT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "SLOT_NOT_AVAILABLE", "선택한 시간은 예약할 수 없습니다."),
	UNSUPPORTED_RESERVATION_TYPE(HttpStatus.BAD_REQUEST, "UNSUPPORTED_RESERVATION_TYPE", "이 장소가 지원하지 않는 예약 유형입니다."),
	INVALID_PACKAGE_TIME(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE_TIME", "패키지 예약의 입실/퇴실 시각이 올바르지 않습니다."),
	INVALID_RESERVATION_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_PERIOD", "체크인/체크아웃 날짜가 올바르지 않습니다."),
	RESERVATION_IN_PAST(HttpStatus.BAD_REQUEST, "RESERVATION_IN_PAST", "지난 시간에는 예약할 수 없습니다."),
	INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_STATUS", "처리할 수 없는 예약 상태입니다."),
	RESERVATION_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "RESERVATION_UPDATE_FAILED", "상태 업데이트에 실패했습니다."),
	RESERVATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "RESERVATION_TOO_SHORT", "예약 시간은 최소 3시간 이상입니다."),
	PET_REQUIRED(HttpStatus.BAD_REQUEST, "PET_REQUIRED", "반려동물 목록은 비어있을 수 없습니다."),
    PET_NOT_OWNED(HttpStatus.BAD_REQUEST, "PET_NOT_OWNED", "본인 소유 반려동물만 등록할 수 있습니다."),
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND","해당 반려동물을 찾을 수 없습니다."),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "해당 예약을 찾을 수 없습니다."),
	HOURLY_PRICE_NOT_SET(HttpStatus.BAD_REQUEST, "HOURLY_PRICE_NOT_SET", "시간당 가격이 설정되어 있지 않습니다."),
	NIGHTLY_PRICE_NOT_SET(HttpStatus.BAD_REQUEST, "NIGHTLY_PRICE_NOT_SET", "1박당 가격이 설정되어 있지 않습니다."),
	RESERVATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RESERVATION_ACCESS_DENIED", "예약에 접근할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
