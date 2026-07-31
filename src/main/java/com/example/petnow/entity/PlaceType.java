package com.example.petnow.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceType {
    APARTMENT("아파트"),
    HOUSE("주택"),
    OFFICETEL("오피스텔");

    private final String label;
}
