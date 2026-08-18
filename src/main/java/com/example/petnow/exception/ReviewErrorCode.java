package com.example.petnow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인의 예약에 대해서만 리뷰를 작성할 수 있습니다."),
    REVIEW_DUPLICATE(HttpStatus.CONFLICT, "REVIEW_DUPLICATE", "이미 해당 예약에 대한 리뷰를 작성했습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

}
