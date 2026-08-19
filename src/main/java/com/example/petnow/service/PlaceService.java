package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;

import java.util.List;

public interface PlaceService {

    void createPlace(Long userId, PlaceCreateRequest request);

    List<PlaceListResponse> getPublishedPlaces();

    PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId);

    /** 홈의 지역 선택지. 공개된 장소가 있는 지역구만 나온다. (#7) */
    List<String> getFilterRegions();

    /**
     * 조건 필터링. 결과 0건은 예외가 아니라 빈 리스트다. (#7)
     *
     * @param loginUserId 비로그인이면 {@code null}. 반려견을 골랐다면 반드시 있어야 한다.
     */
    PlaceSearchResponse searchPlaces(Long loginUserId, PlaceFilterRequest request);
}
