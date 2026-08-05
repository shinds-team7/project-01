package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.UserService;
import jakarta.servlet.http.HttpSession;
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
    private final UserService userService;

    @GetMapping
    public String myPage(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        model.addAttribute("petList", petService.getPetList(userId));

        UserMyPageResponse user = userService.getMyPage(userId);
        model.addAttribute("user", user);

        return "mypage/index";
    }
}