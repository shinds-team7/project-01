package com.example.petnow.controller;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/host/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/create")
    public String createForm(Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("loginUserId");
        if (loginUserId == null) {
            return "redirect:/";
        }

        model.addAttribute("placeCreateRequest", new PlaceCreateRequest());
        addCreateFormAttributes(model);

        return "host/places/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("placeCreateRequest") PlaceCreateRequest requestDTO,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session) {

        Long loginUserId = (Long) session.getAttribute("loginUserId");
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            addCreateFormAttributes(model);
            return "host/places/create";
        }

        placeService.createPlace(loginUserId, requestDTO);

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
