package com.example.petnow.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceStatus {
    PENDING("승인 대기"),
    PUBLISHED("게시");

    private final String label;
}
