package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    boolean existsByUserAndPlace(@Param("userId") Long userId,
                                 @Param("placeId") Long placeId);

    int insert(@Param("userId") Long userId,
               @Param("placeId") Long placeId);

    int delete(@Param("userId") Long userId,
               @Param("placeId") Long placeId);

    List<PlaceListResponse> findPlacesByUserId(@Param("userId") Long userId);
}
