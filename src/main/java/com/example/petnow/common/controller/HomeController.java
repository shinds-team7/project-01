package com.example.petnow.common.controller;

import com.example.petnow.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /** 홈의 가로 스크롤 한 줄에 들어갈 만큼만 보낸다. 장소가 늘어도 홈 응답이 커지지 않게 한다. */
    private static final int RECENT_PLACES_LIMIT = 8;

    private final PlaceService placeService;

    /** 루트 접속 시 앱 홈으로 보낸다. */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /** 앱 홈. 프로토타입의 HOME 화면. */
    @GetMapping("/home")
    public String home(Model model) {
        // 조회 이력 API 가 없어 공개된 장소를 대신 보여준다. 이력 API 가 붙으면 교체한다.
        model.addAttribute("recentPlaces", placeService.getPublishedPlaces().stream()
                .limit(RECENT_PLACES_LIMIT)
                .toList());
        return "home";
    }

    /**
     * 하단 네비의 목적지 중 아직 화면이 없는 항목을 채워두는 임시 화면.
     * 해당 화면이 만들어지면 이 매핑과 함께 지운다.
     */
    @GetMapping("/search")
    public String search(Model model) {
        return comingSoon(model, "검색", "search");
    }

    /**
     * 내 주변. 프로토타입의 NEARBY 화면.
     *
     * <p>거리순 정렬은 아직 못 한다. {@code place_addresses} 에 위경도 컬럼이 있지만
     * 엔티티·매퍼가 없어 자바에서 좌표를 읽을 수 없다. 그때까지는 공개된 장소를
     * 그대로 보여주고, 화면이 안내 문구로 그 사실을 알린다.
     */
    @GetMapping("/nearby")
    public String nearby(Model model) {
        model.addAttribute("places", placeService.getPublishedPlaces());
        return "nearby";
    }

    @GetMapping("/bookmarks")
    public String bookmarks(Model model) {
        return comingSoon(model, "찜한 호스트", "like");
    }

    private String comingSoon(Model model, String title, String navKey) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("navKey", navKey);
        return "coming-soon";
    }
}
