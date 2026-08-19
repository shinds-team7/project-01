package com.example.petnow.common.controller;

import com.example.petnow.service.PlaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** 홈의 가로 스크롤 한 줄에 들어갈 만큼만 보낸다. 장소가 늘어도 홈 응답이 커지지 않게 한다. */
    private static final int RECENT_PLACES_LIMIT = 8;

    private final PlaceService placeService;

    /**
     * 카카오맵 JavaScript 키. 브라우저가 쓰는 키라 화면에 그대로 실린다.
     *
     * <p>{@code @ControllerAdvice} 로 전역에 뿌리지 않고 이 컨트롤러가 {@code /nearby} 모델에만 담는다.
     * 지도를 쓰지 않는 화면까지 키를 달고 다닐 이유가 없다.
     *
     * <p>서버 전용 REST API 키({@code app.kakao.rest-api-key})는 여기서 읽지 않는다. 웹 계층이
     * 아예 모르는 값이어야 실수로 내려보낼 수 없다. 두 키가 뒤바뀐 경우는
     * {@link com.example.petnow.common.config.KakaoKeyGuard} 가 기동 때 잡는다.
     */
    private final String kakaoMapJavascriptKey;

    public HomeController(PlaceService placeService,
                          @Value("${app.kakao.map.javascript-key:}") String kakaoMapJavascriptKey) {
        this.placeService = placeService;
        this.kakaoMapJavascriptKey = kakaoMapJavascriptKey == null ? "" : kakaoMapJavascriptKey.trim();
    }

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
     * 내 주변. 프로토타입의 NEARBY 화면. (#277)
     *
     * <p>장소마다 좌표가 실려 나가지만 {@code null} 일 수 있다. 좌표가 있는 곳만 지도 마커가 되고,
     * 없는 곳도 목록에는 그대로 남는다. 거리 계산·정렬은 사용자 좌표를 아는 브라우저가 한다.
     *
     * <p>{@code kakaoMapEnabled} 가 {@code false} 면 화면이 지도 SDK 를 아예 부르지 않는다.
     * 키 없이 SDK 를 부르면 401 만 찍히고 지도 자리는 어차피 비어 있다.
     */
    @GetMapping("/nearby")
    public String nearby(Model model) {
        model.addAttribute("places", placeService.getPublishedPlaces());
        model.addAttribute("kakaoMapJavascriptKey", kakaoMapJavascriptKey);
        model.addAttribute("kakaoMapEnabled", !kakaoMapJavascriptKey.isEmpty());
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
