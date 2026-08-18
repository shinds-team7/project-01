package com.example.petnow.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Getter
@Setter
public class PlaceOperatingPolicyUpdateRequest {
    private boolean supportsHourly;
    private boolean supportsPackage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime packageCheckInTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime packageCheckOutTime;
}
