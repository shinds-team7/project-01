package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.entity.PlaceAvailability;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlaceAvailabilityMapper {

    int insertSlots(@Param("slots") List<PlaceAvailability> slots);

    /**
     * 지정 구간의 OPEN 슬롯을 RESERVED 로 바꾸고 변경된 행 수를 돌려준다.
     * 호출한 쪽은 반환값이 필요한 슬롯 개수와 같은지 반드시 확인해야 한다.
     */
    int updateSlotsToReserved(@Param("placeId") Long placeId,
                              @Param("startAt") LocalDateTime startAt,
                              @Param("endAt") LocalDateTime endAt);

    int updateSlotsToOpen(@Param("slotIds") List<Long> slotIds);

    int updateSlotStatus(@Param("slotId") Long slotId,
                         @Param("status") String status);

    List<Long> findSlotIdsByRange(@Param("placeId") Long placeId,
                                  @Param("startAt") LocalDateTime startAt,
                                  @Param("endAt") LocalDateTime endAt);

    List<PlaceSlotResponse> findSlotsByPlaceAndPeriod(@Param("placeId") Long placeId,
                                                      @Param("fromAt") LocalDateTime fromAt,
                                                      @Param("toAt") LocalDateTime toAt);

    int countOpenSlotsInRange(@Param("placeId") Long placeId,
                              @Param("startAt") LocalDateTime startAt,
                              @Param("endAt") LocalDateTime endAt);

    LocalDateTime findLastSlotEndAt(@Param("placeId") Long placeId);
}
