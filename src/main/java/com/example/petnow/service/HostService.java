package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.HostPlaceListResponse;

import java.util.List;

public interface HostService {
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
    List<HostPlaceListResponse> getPlacesByUserId(Long userId);

    /**
     * 호스트 본인의 장소를 소프트 삭제한다.
     *
     * @param userId 현재 호스트의 ID
     * @param placeId 삭제할 장소의 ID
     * @throws com.example.petnow.exception.BusinessException 내 장소가 아니거나 이미 삭제된 경우
     */
    void deletePlace(Long userId, Long placeId);

}
