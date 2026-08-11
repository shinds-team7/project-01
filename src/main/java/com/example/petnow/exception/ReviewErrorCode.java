package com.example.petnow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인의 예약에 대해서만 리뷰를 작성할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

}