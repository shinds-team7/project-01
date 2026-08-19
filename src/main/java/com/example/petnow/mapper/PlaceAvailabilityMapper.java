package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.entity.PlaceAvailability;
import com.example.petnow.entity.SlotStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlaceAvailabilityMapper {

    int insertSlots(List<PlaceAvailability> slots);

    int updateSlotsToOpen(List<Long> slotIds);

    List<PlaceSlotResponse> findSlotsByPlaceAndPeriod(@Param("placeId") Long placeId,
                                                      @Param("fromAt") LocalDateTime fromAt,
                                                      @Param("toAt") LocalDateTime toAt);

    PlaceSlotPeriodResponse findSlotPeriodByPlaceId(Long placeId);

    int updateSlotStatus(@Param("placeId") Long placeId,
                         @Param("slotId") Long slotId,
                         @Param("status") SlotStatus status);
}
