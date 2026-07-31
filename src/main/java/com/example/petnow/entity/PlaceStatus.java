package com.example.petnow.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceStatus {
    DRAFT("임시저장"),
    PUBLISHED("게시"),
    HIDDEN("숨김");

    private final String label;
}
