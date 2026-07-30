package com.example.petnow.controller;

import com.example.petnow.dto.UserSignupRequest;
import com.example.petnow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public String addUser(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        return "mypage";
    }
}
