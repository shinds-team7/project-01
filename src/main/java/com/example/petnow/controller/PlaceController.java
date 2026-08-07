package com.example.petnow.controller;

import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("places", placeService.getPublishedPlaces());

        return "places/list";
    }

    @GetMapping("/{placeId}")
    public String detail(@PathVariable Long placeId, Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("loginUserId");

        model.addAttribute("place", placeService.getPlaceDetail(placeId, loginUserId));

        return "place-detail";
    }
}
