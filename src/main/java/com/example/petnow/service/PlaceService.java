package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;

import java.util.List;

public interface PlaceService {
    /**
     * 호스트의 장소 게시글을 등록한다.
     *
     * @param userId 장소를 등록하는 호스트의 ID
     * @param request 장소 등록 요청 정보
     */
    void createPlace(Long userId, PlaceCreateRequest request);

    /**
     *
     * @param userId 현재 호스트의 ID
     * @return 호스트 ID로 조회되는 장소정보의 리스트형
     */
    List<PlaceListResponse> getPlacesByUserId(Long userId);

    PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId);
}
