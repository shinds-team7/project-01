package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.service.PetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;

    @GetMapping("/new")
    public String newPetForm(HttpSession session) {
        if (session.getAttribute(SessionConst.LOGIN_USER_ID) == null) {
            return "redirect:/";
        }
        return "pet-form";
    }

    @PostMapping("/create")
    public String addPet(@ModelAttribute PetCreateRequest createRequest, HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        if (userId == null) {
            return "redirect:/";
        }
        petService.createPet(userId, createRequest);
        return "redirect:/mypage#pets";
    }

    @PostMapping("/update")
    public String updatePet(@ModelAttribute PetUpdateRequest updateRequest) {
        petService.updatePet(3L, updateRequest);
        return "redirect:/mypage#pets";
    }
}
