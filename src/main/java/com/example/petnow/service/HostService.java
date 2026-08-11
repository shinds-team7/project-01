package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.HostPlaceListResponse;

import java.util.List;

public interface HostService {

    void createPlace(Long userId, PlaceCreateRequest request);

    List<HostPlaceListResponse> getPlacesByUserId(Long userId);
}
