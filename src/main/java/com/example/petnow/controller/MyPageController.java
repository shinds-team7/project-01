package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.AuthService;
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
    private final AuthService authService;

    @GetMapping
    public String myPage(@LoginUser Long loginUserId, Model model) {

        model.addAttribute("petList", petService.getPetList(loginUserId));

        UserMyPageResponse user = authService.getMyPage(loginUserId);
        model.addAttribute("user", user);

        return "mypage";
    }
}
