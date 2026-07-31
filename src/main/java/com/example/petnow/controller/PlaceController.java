package com.example.petnow.controller;

import com.example.petnow.dto.request.PlaceCreateRequestDTO;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/host/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(
                "placeCreateRequestDTO",
                new PlaceCreateRequestDTO()
        );
        addCreateFormAttributes(model);

        return "host/places/create";
    }

    //TODO: 로그인 구현 후 세션 사용자 ID로 교체
    @PostMapping
    public String create(@Valid PlaceCreateRequestDTO requestDTO, BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addCreateFormAttributes(model);
            return "host/places/create";
        }

        Long userId = 1L;
        placeService.createPlace(userId, requestDTO);

        return "redirect:/host/places/success";
    }

    @GetMapping("/success")
    public String createSuccess() {
        return "host/places/success";
    }

    private void addCreateFormAttributes(Model model) {
        model.addAttribute("placeTypes", PlaceType.values());
    }
}
