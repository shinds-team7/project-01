package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.entity.Place;
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
        Long hostUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (hostUserId == null) {
            return "redirect:/";
        }

        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("place", place);
        model.addAttribute("targetDate", targetDate);
        model.addAttribute("slots", placeAvailabilityService.getSlotsOfDay(placeId, targetDate));
        return "host/availability";
    }

    @PostMapping
    public String openSlots(@PathVariable Long placeId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                            HttpSession session) {
        Long hostUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (hostUserId == null) {
            return "redirect:/";
        }

        placeAvailabilityService.openSlots(placeId, fromDate, toDate);
        return "redirect:/host/places/" + placeId + "/availability?date=" + fromDate;
    }

    @PostMapping("/status")
    public String changeStatus(@PathVariable Long placeId,
                               @RequestParam Long slotId,
                               @RequestParam String status,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               HttpSession session) {
        Long hostUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (hostUserId == null) {
            return "redirect:/";
        }

        placeAvailabilityService.changeSlotStatus(placeId, slotId, status, hostUserId);
        return "redirect:/host/places/" + placeId + "/availability?date=" + date;
    }
}
