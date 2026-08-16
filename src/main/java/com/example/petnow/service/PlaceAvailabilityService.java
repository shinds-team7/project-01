package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceOperatingPolicyUpdateRequest;
import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.entity.SlotStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface PlaceAvailabilityService {

    int createSlots(Long hostUserId, Long placeId, LocalDate fromDate, LocalDate toDate);

    List<PlaceSlotResponse> getHostSlots(Long hostUserId, Long placeId, LocalDate date);

    PlaceSlotPeriodResponse getSlotPeriod(Long hostUserId, Long placeId);

    void changeSlotStatus(Long hostUserId, Long placeId, Long slotId, SlotStatus status);

    void updateOperatingPolicy(Long hostUserId,
                               Long placeId,
                               PlaceOperatingPolicyUpdateRequest request);

    List<PlaceSlotResponse> getHourlySlots(Long placeId, LocalDate date);

    List<PackageDayResponse> getPackageDays(Long placeId, YearMonth yearMonth);
}
