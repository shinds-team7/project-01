package com.example.petnow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

    PLACE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLACE_NOT_FOUND",
            "존재하지 않거나 조회할 수 없는 장소입니다."
    ),

    PLACE_CREATE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "PLACE_CREATE_FAILED",
            "장소 등록에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;
}
