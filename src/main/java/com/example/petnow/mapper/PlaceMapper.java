package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalTime;
import java.util.List;

@Mapper
public interface PlaceMapper {
    int insert(Place place);

    int insertAddress(@Param("placeId") Long placeId,
                      @Param("sido") String sido,
                      @Param("sigungu") String sigungu,
                      @Param("roadAddress") String roadAddress);

    Place findById(Long placeId);

    List<PlaceListResponse> findAllPublished();

    PlaceDetailResponse findDetailById(Long placeId);

    int updateOperatingPolicy(@Param("placeId") Long placeId,
                              @Param("supportsHourly") boolean supportsHourly,
                              @Param("supportsPackage") boolean supportsPackage,
                              @Param("packageCheckInTime") LocalTime packageCheckInTime,
                              @Param("packageCheckOutTime") LocalTime packageCheckOutTime);

    void updateAvgRating(Long placeId);
}
