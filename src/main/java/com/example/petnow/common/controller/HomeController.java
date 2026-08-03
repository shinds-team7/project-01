package com.example.petnow.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** 루트 접속 시 장소 목록으로 보낸다. */
    @GetMapping("/")
    public String home() {
        return "redirect:/places";
    }
}
