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
    ),

    PLACE_UPDATE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "PLACE_UPDATE_FAILED",
            "장소 수정에 실패했습니다."
    ),

    PLACE_PHOTO_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PLACE_PHOTO_NOT_FOUND",
            "해당 장소에 등록된 사진을 찾을 수 없습니다."
    ),

    PLACE_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "PLACE_ACCESS_DENIED",
            "해당 장소를 관리할 권한이 없습니다"
    ),

    PLACE_AVAILABILITY_PERIOD_INVALID(
            HttpStatus.BAD_REQUEST,
            "PLACE_AVAILABILITY_PERIOD_INVALID",
            "슬롯 생성 기간이 올바르지 않습니다"
    ),

    PLACE_SLOT_STATUS_INVALID(
            HttpStatus.BAD_REQUEST,
            "PLACE_SLOT_STATUS_INVALID",
            "변경할 수 없는 슬롯 상태입니다"
    ),

    PLACE_SLOT_UPDATE_FAILED(
            HttpStatus.BAD_REQUEST,
            "PLACE_SLOT_UPDATE_FAILED",
            "슬롯 상태를 변경할 수 없습니다"
    ),

    PLACE_PACKAGE_TIME_INVALID(
            HttpStatus.BAD_REQUEST,
            "PLACE_PACKAGE_TIME_INVALID",
            "패키지 입실과 퇴실 시각은 3시간 격자에 맞아야 합니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;
}
