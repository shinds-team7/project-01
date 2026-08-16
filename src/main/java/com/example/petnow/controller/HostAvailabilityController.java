package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PlaceOperatingPolicyUpdateRequest;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.SlotStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.PlaceAvailabilityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/host/places/{placeId}/availability")
@RequiredArgsConstructor
public class HostAvailabilityController {

    private final PlaceAvailabilityService placeAvailabilityService;
    private final PlaceMapper placeMapper;

    @GetMapping
    public String availability(@PathVariable Long placeId,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               HttpSession session,
                               Model model) {
        Long hostUserId = getLoginUserId(session);
        if (hostUserId == null) {
            return "redirect:/";
        }

        Place place = findOwnedPlace(hostUserId, placeId);
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        model.addAttribute("place", place);
        model.addAttribute("targetDate", targetDate);
        model.addAttribute("slots", placeAvailabilityService.getHostSlots(hostUserId, placeId, targetDate));
        model.addAttribute("policyRequest", createPolicyRequest(place));
        PlaceSlotPeriodResponse slotPeriod = placeAvailabilityService.getSlotPeriod(hostUserId, placeId);
        model.addAttribute("slotFromDate", slotPeriod == null || slotPeriod.getFromDate() == null
                ? targetDate : slotPeriod.getFromDate());
        model.addAttribute("slotToDate", slotPeriod == null || slotPeriod.getToDate() == null
                ? targetDate.plusDays(6) : slotPeriod.getToDate());
        return "host/availability";
    }

    @PostMapping
    public String createSlots(@PathVariable Long placeId,
                              @RequestParam
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                              @RequestParam
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                              HttpSession session) {
        Long hostUserId = getLoginUserId(session);
        if (hostUserId == null) {
            return "redirect:/";
        }
        placeAvailabilityService.createSlots(hostUserId, placeId, fromDate, toDate);
        return redirectToAvailability(placeId, fromDate);
    }

    @PostMapping("/status")
    public String changeStatus(@PathVariable Long placeId,
                               @RequestParam Long slotId,
                               @RequestParam SlotStatus status,
                               @RequestParam
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               HttpSession session) {
        Long hostUserId = getLoginUserId(session);
        if (hostUserId == null) {
            return "redirect:/";
        }
        placeAvailabilityService.changeSlotStatus(hostUserId, placeId, slotId, status);
        return redirectToAvailability(placeId, date);
    }

    @PostMapping("/policy")
    public String updatePolicy(@PathVariable Long placeId,
                               @ModelAttribute PlaceOperatingPolicyUpdateRequest request,
                               @RequestParam
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               HttpSession session) {
        Long hostUserId = getLoginUserId(session);
        if (hostUserId == null) {
            return "redirect:/";
        }
        placeAvailabilityService.updateOperatingPolicy(hostUserId, placeId, request);
        return redirectToAvailability(placeId, date);
    }

    private PlaceOperatingPolicyUpdateRequest createPolicyRequest(Place place) {
        PlaceOperatingPolicyUpdateRequest request = new PlaceOperatingPolicyUpdateRequest();
        request.setSupportsHourly(place.isSupportsHourly());
        request.setSupportsPackage(place.isSupportsPackage());
        request.setPackageCheckInTime(place.getPackageCheckInTime());
        request.setPackageCheckOutTime(place.getPackageCheckOutTime());
        return request;
    }

    private Place findOwnedPlace(Long hostUserId, Long placeId) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        if (!hostUserId.equals(place.getHostUserId())) {
            throw new BusinessException(PlaceErrorCode.PLACE_ACCESS_DENIED);
        }
        return place;
    }

    private Long getLoginUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
    }

    private String redirectToAvailability(Long placeId, LocalDate date) {
        return "redirect:/host/places/" + placeId + "/availability?date=" + date;
    }
}
