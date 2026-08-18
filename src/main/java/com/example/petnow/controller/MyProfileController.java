package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.service.MyProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyProfileController {

    private final MyProfileService myProfileService;

    @GetMapping("/profile")
    // 프로필 상세 조회
    public String getProfile(@LoginUser Long loginUserId, Model model) {

        MyProfileResponse profile = myProfileService.getProfile(loginUserId);

        model.addAttribute("profile", profile);

        return "mypage/profile";
    }


    // 회원탈퇴 , 페이지 생성 후 버튼, form 연결 필요
    @PostMapping("/withdraw")
    public String withdraw(@LoginUser Long loginUserId, HttpSession session) {

        myProfileService.withdraw(loginUserId);

        LoginSession.clear(session);

        return "redirect:/home";
    }

}
