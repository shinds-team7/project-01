package com.example.petnow.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    IMAGE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "IMAGE_TYPE_NOT_ALLOWED", "jpg, png, webp 이미지만 올릴 수 있습니다"),
    IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "IMAGE_COUNT_EXCEEDED", "올릴 수 있는 이미지 개수를 초과했습니다"),
    IMAGE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "IMAGE_TOO_LARGE", "이미지는 한 장에 5MB 까지 올릴 수 있습니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;
}
