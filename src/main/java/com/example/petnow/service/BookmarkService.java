package com.example.petnow.service;

import com.example.petnow.dto.response.PlaceListResponse;

import java.util.List;

public interface BookmarkService {

    boolean toggle(Long userId, Long placeId);

    boolean isBookmarked(Long userId, Long placeId);

    List<PlaceListResponse> getBookmarkedPlaces(Long userId);
}
