package com.example.petnow.service;

import com.example.petnow.dto.PlaceCreateRequestDTO;

public interface PlaceService {
    Long createPlace(Long userId, PlaceCreateRequestDTO requestDTO);
}
