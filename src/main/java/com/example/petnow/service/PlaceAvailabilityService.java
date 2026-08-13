package com.example.petnow.service;

import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PlaceSlotResponse;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface PlaceAvailabilityService {

    int openSlots(Long placeId, LocalDate fromDate, LocalDate toDate);

    List<PlaceSlotResponse> getHourlySlots(Long placeId, LocalDate date);

    List<PackageDayResponse> getPackageDays(Long placeId, YearMonth yearMonth);

    List<PlaceSlotResponse> getSlotsOfDay(Long placeId, LocalDate date);

    void changeSlotStatus(Long placeId, Long slotId, String status, Long hostUserId);
}
