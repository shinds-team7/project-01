package com.example.petnow.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotStatus {
    OPEN("예약 가능"),
    BLOCKED("예약 불가"),
    RESERVED("예약 완료");

    private final String label;
}
