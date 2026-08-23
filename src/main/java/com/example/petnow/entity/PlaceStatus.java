package com.example.petnow.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceStatus {
    PENDING("임시저장"),
    PUBLISHED("게시 중");

    private final String label;
}
