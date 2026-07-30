package com.example.petnow.controller;

import com.example.petnow.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final PetService petService;

    @GetMapping
    public String myPage(Model model) {

        // TODO 로그인 구현 되었을때 사용 petService.petlist(user.getId())
        model.addAttribute("petList", petService.getPetList(1L));

        return "mypage/index";
    }
}