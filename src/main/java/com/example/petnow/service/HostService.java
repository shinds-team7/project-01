package com.example.petnow.service;

import com.example.petnow.dto.response.HostPlaceListResponse;

import java.util.List;

public interface HostService {

    List<HostPlaceListResponse> getPlacesByUserId(Long userId);
}
