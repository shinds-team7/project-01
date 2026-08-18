package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameAvailabilityResponse {

    private final boolean available;
    private final String message;
}
