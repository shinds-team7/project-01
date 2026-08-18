package com.example.petnow.controller;

import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.service.PlaceAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceSlotController {

    private final PlaceAvailabilityService placeAvailabilityService;

    @GetMapping("/{placeId}/slots")
    @ResponseBody
    public List<PlaceSlotResponse> hourlySlots(
            @PathVariable Long placeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return placeAvailabilityService.getHourlySlots(placeId, date);
    }

    @GetMapping("/{placeId}/package-days")
    @ResponseBody
    public List<PackageDayResponse> packageDays(
            @PathVariable Long placeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return placeAvailabilityService.getPackageDays(placeId, yearMonth);
    }
}
