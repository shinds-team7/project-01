package com.example.petnow.mapper;

import com.example.petnow.dto.request.PlaceFilterCriteria;
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

    /** 조건 필터링 결과. 지역·마릿수·일정 조건을 쿼리 한 번으로 건다. (#7) */
    List<PlaceListResponse> findByFilter(PlaceFilterCriteria criteria);

    /** 지역 선택지. 공개된 장소가 실제로 있는 지역구만 돌려준다. (#7) */
    List<String> findFilterableSigungu();
}
