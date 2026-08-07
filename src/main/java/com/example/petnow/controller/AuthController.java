package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 , 메인페이지 생성 되면 return을 메인페이지로 바꿀 예정
    @PostMapping("/signup")
    public String userSignup(@Valid @ModelAttribute UserSignupRequest request) {
        authService.signup(request);
        return "redirect:/auth/login";
    }

    // 로그인 , 메인페이지 생성 되면 return을 메인페이지로 바꿀 예정
    @PostMapping ("/login")
    public String userLogin(@Valid @ModelAttribute UserLoginRequest request, HttpSession session) {

        Long userId = authService.login(request);

        session.setAttribute(SessionConst.LOGIN_USER_ID, userId);

        // 세션 저장 test
        log.info("세션에 저장된 userId: {}",
                session.getAttribute(SessionConst.LOGIN_USER_ID));

        return "redirect:/mypage";
    }
}
