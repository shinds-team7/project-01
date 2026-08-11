package com.example.petnow.mapper;

import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HostMapper {

    int insert(Place place);

    List<HostPlaceListResponse> findAllByUserId(@Param("userId") Long userId);
}
