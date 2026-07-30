package com.example.petnow.controller;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public String addUser(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        return "mypage";
    }

    // 로그인
    @PostMapping ("/login")
    public String userLogin(@RequestBody UserLoginRequest request) {
        userService.login(request);
        return "mypage";
    }
}
