package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequestDTO;
import com.example.petnow.dto.response.PlaceListResponseDTO;

import java.util.List;

public interface PlaceService {
    /*
    * 장소 게시글을 등록, 반환 타입은 없어요
    * Mapper 의 insert와 연결되어있어요
    * throw IllegalStateException 은 id 가 null 이거나 insert 결과가 1행보다 작을때
    */
    void createPlace(Long userId, PlaceCreateRequestDTO requestDTO);

    List<PlaceListResponseDTO> getPlacesByUserId(Long userId);

    //loadPlace
}
