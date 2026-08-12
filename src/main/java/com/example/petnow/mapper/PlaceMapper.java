package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlaceMapper {
    Place findById(Long placeId);

    List<PlaceListResponse> findAllPublished();

    PlaceDetailResponse findDetailById(Long placeId);
}
