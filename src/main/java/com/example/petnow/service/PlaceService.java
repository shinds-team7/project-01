package com.example.petnow.service;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;

import java.util.List;

public interface PlaceService {

    List<PlaceListResponse> getPublishedPlaces();

    PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId);
}
