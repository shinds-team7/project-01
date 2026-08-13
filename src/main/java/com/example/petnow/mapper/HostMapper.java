package com.example.petnow.mapper;

import com.example.petnow.dto.response.HostPlaceListResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HostMapper {

    List<HostPlaceListResponse> findAllByUserId(Long userId);
}
