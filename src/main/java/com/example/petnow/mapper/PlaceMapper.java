package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceListResponseDTO;
import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaceMapper {
    int insert(Place place);

    List<PlaceListResponseDTO> findAllByUserId(@Param("userId") Long userId);
}
