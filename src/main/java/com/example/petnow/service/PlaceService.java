package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.PlaceListResponseDTO;

import java.util.List;

public interface PlaceService {
    /**
     * 호스트의 장소 게시글을 등록한다.
     *
     * @param userId 장소를 등록하는 호스트의 ID
     * @param requestDTO 장소 등록 요청 정보
     */
    void createPlace(Long userId, PlaceCreateRequest requestDTO);

    List<PlaceListResponseDTO> getPlacesByUserId(Long userId);
}
