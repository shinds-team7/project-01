package com.example.petnow.common.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** 앱 홈. 프로토타입의 HOME 화면. */
    @GetMapping("/")
    public String home(Model model) {
        // 공개 장소 목록 조회는 검색 화면 작업에서 함께 추가한다. 그 전까지는 빈 상태로 그린다.
        model.addAttribute("recentPlaces", List.of());
        return "home";
    }

    @GetMapping("/search")
    public String search(Model model) {
        return comingSoon(model, "검색", "search");
    }

    @GetMapping("/places")
    public String places(Model model) {
        return comingSoon(model, "펫시터 찾기", "search");
    }

    @GetMapping("/nearby")
    public String nearby(Model model) {
        return comingSoon(model, "내 주변", "nearby");
    }

    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        return comingSoon(model, "찜한 호스트", "like");
    }

    /**
     * 검색·장소·찜 화면이 만들어지기 전까지 하단 네비의 목적지를 채워두는 임시 화면.
     * 해당 화면들이 붙으면 위 매핑과 함께 지운다.
     */
    private String comingSoon(Model model, String title, String navKey) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("navKey", navKey);
        return "coming-soon";
    }
}
