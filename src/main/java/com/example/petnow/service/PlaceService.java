package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;

import java.util.List;

public interface PlaceService {

    void createPlace(Long userId, PlaceCreateRequest request);

    List<PlaceListResponse> getPublishedPlaces();

    PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId);
}
