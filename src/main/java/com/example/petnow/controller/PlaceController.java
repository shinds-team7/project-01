package com.example.petnow.controller;

import com.example.petnow.dto.PlaceCreateRequestDTO;
import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;


    @GetMapping("/create")
    public String createForm() {

        return "host/places/create";
    }

    //TODO: 로그인 구현 후 세션 사용자 ID로 교체
    @PostMapping
    public String create(@ModelAttribute PlaceCreateRequestDTO requestDTO,HttpSession session) {

        Long userId = 1L;
        Long placeId = placeService.createPlace(userId, requestDTO);

        return "redirect:/places/" + placeId;
    }
}
