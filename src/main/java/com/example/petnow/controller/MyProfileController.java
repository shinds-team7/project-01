package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.service.MyProfileService;
import com.example.petnow.service.MyProfileServiceImpl;
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

    private final MyProfileServiceImpl myProfileServiceImpl;
    private final MyProfileService myProfileService;

    @GetMapping("/profile")
    // 프로필 상세 조회
    public String getProgile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        if(userId == null) {
            return "redirect:/auth/login";
        }

        MyProfileResponse profile = myProfileServiceImpl.getProfile(userId);

        model.addAttribute("profile", profile);

        // 페이지가 아직 없어, 페이지 생성 후 return값 변경할 예정입니다
        return "mypage/myprofile-test";
    }


    // 회원탈퇴 , 페이지 생성 후 버튼, form 연결 필요
    @PostMapping("/withdraw")
    public String withdrqw(HttpSession session) {

        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        myProfileService.withdraw(userId);

        LoginSession.clear(session);

        return "redirect:/home";
    }

}
