package com.example.petnow.controller;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 , 메인페이지 생성 되면 return을 메인페이지로 바꿀 예정
    @PostMapping("/signup")
    public String userSignup(@Valid @ModelAttribute UserSignupRequest request) {
        userService.signup(request);
        return "mypage";
    }

    // 로그인 , 메인페이지 생성 되면 return을 메인페이지로 바꿀 예정
    @PostMapping ("/login")
    public String userLogin(@Valid @ModelAttribute UserLoginRequest request,
                            HttpSession session) {

        Long userId = userService.login(request);

        session.setAttribute("loginUserId", userId);

        return "mypage";
    }
}
