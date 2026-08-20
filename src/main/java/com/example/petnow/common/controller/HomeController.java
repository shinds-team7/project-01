package com.example.petnow.common.controller;

import com.example.petnow.common.session.LoginRedirect;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    /** 홈의 가로 스크롤 한 줄에 들어갈 만큼만 보낸다. 장소가 늘어도 홈 응답이 커지지 않게 한다. */
    private static final int RECENT_PLACES_LIMIT = 8;

    /**
     * 타입 변환이 실패한 필드의 안내 문구. 스프링 기본 문구는 영어라 화면에 그대로 쓸 수 없다.
     * 여기 없는 필드는 "조건" 이라는 일반 명칭으로 안내한다.
     */
    private static final Map<String, String> FILTER_FIELD_LABELS = Map.of(
            "startDate", "시작 날짜",
            "endDate", "종료 날짜",
            "startTime", "시작 시간",
            "endTime", "종료 시간",
            "petIds", "반려동물",
            "placeType", "장소 유형",
            "regions", "지역"
    );

    private final PlaceService placeService;
    private final PetService petService;

    /**
     * 카카오맵 JavaScript 키. 브라우저가 쓰는 키라 화면에 그대로 실린다.
     *
     * <p>{@code @ControllerAdvice} 로 전역에 뿌리지 않고 이 컨트롤러가 {@code /nearby} 모델에만 담는다.
     * 지도를 쓰지 않는 화면까지 키를 달고 다닐 이유가 없다.
     *
     * <p>서버 전용 REST API 키({@code kakao.local-api.rest-api-key})는 여기서 읽지 않는다. 웹 계층이
     * 아예 모르는 값이어야 실수로 내려보낼 수 없다. 두 키가 뒤바뀐 경우는
     * {@link com.example.petnow.common.config.KakaoKeyGuard} 가 기동 때 잡는다.
     */
    private final String kakaoMapJavascriptKey;

    /**
     * {@code @RequiredArgsConstructor} 를 쓰지 않는다. lombok 이 만든 생성자에는 {@code @Value} 가
     * 옮겨 붙지 않아(복사할 애너테이션을 지정하는 lombok.config 가 이 저장소에 없다) 키가 주입되지 않는다.
     */
    public HomeController(PlaceService placeService,
                          PetService petService,
                          @Value("${kakao.map.javascript-key:}") String kakaoMapJavascriptKey) {
        this.placeService = placeService;
        this.petService = petService;
        this.kakaoMapJavascriptKey = kakaoMapJavascriptKey == null ? "" : kakaoMapJavascriptKey.trim();
    }

    /** 루트 접속 시 앱 홈으로 보낸다. */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /** 앱 홈. 프로토타입의 HOME 화면. */
    @GetMapping("/home")
    public String home(HttpServletRequest request, Model model) {
        // 조회 이력 API 가 없어 공개된 장소를 대신 보여준다. 이력 API 가 붙으면 교체한다.
        model.addAttribute("recentPlaces", placeService.getPublishedPlaces().stream()
                .limit(RECENT_PLACES_LIMIT)
                .toList());

        // 검색 조건 카드의 다이얼로그를 채운다. 지역은 아직 place_addresses 가 비어 있으면 0건이다. (#7)
        model.addAttribute("regions", placeService.getFilterRegions());
        Long loginUserId = LoginSession.currentUserId(request);
        model.addAttribute("myPets", loginUserId == null ? List.of() : petService.getPetList(loginUserId));
        return "home";
    }

    /** 장소명·소개글·호스트 닉네임으로 공개된 공간을 찾는다. (#264) */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         HttpServletRequest request,
                         Model model) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        boolean searched = !normalizedKeyword.isEmpty();

        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("searched", searched);
        if (!searched) {
            model.addAttribute("places", List.of());
            return "places/search";
        }

        PlaceFilterRequest filter = new PlaceFilterRequest();
        filter.setKeyword(normalizedKeyword);
        PlaceSearchResponse result = placeService.searchPlaces(LoginSession.currentUserId(request), filter);
        model.addAttribute("places", result.getPlaces());
        return "places/search";
    }

    /**
     * 내 주변. 홈의 검색 조건 카드가 제출하는 조건 필터링 결과 화면이다. (#7)
     *
     * <p>조건을 하나도 안 보내면 예전처럼 공개된 장소 전체가 나온다. 조건이 잘못됐을 때는
     * 500 이 아니라 폼 에러 문구와 함께 이 화면을 다시 그린다. 결과 0건도 예외가 아니다.
     *
     * <p>장소마다 좌표가 실려 나가지만 {@code null} 일 수 있다. 좌표가 있는 곳만 지도 마커가 되고,
     * 없는 곳도 목록에는 그대로 남는다. 거리 계산·정렬은 사용자 좌표를 아는 브라우저가 한다. (#277)
     *
     * <p>{@code kakaoMapEnabled} 가 {@code false} 면 화면이 지도 SDK 를 아예 부르지 않는다.
     * 키 없이 SDK 를 부르면 401 만 찍히고 지도 자리는 어차피 비어 있다.
     */
    @GetMapping("/nearby")
    public String nearby(@Valid @ModelAttribute("placeFilter") PlaceFilterRequest filter,
                         BindingResult bindingResult,
                         HttpServletRequest request,
                         Model model) {
        // 반려견 조건은 내 아이 목록이 있어야 한다. 비로그인이면 로그인 후 이 검색으로 되돌아온다.
        if (filter.hasPetSelection() && !LoginSession.isLoggedIn(request)) {
            LoginRedirect.save(request);
            return "redirect:/auth/login";
        }

        // 조건 오류로 다시 그리는 경우에도 지도는 떠야 한다. 여기서 한 번만 담고 두 갈래가 같이 쓴다.
        model.addAttribute("kakaoMapJavascriptKey", kakaoMapJavascriptKey);
        model.addAttribute("kakaoMapEnabled", !kakaoMapJavascriptKey.isEmpty());

        if (bindingResult.hasErrors()) {
            model.addAttribute("filterErrors", filterErrors(bindingResult));
            model.addAttribute("places", List.of());
            model.addAttribute("filtered", false);
            return "nearby";
        }

        PlaceSearchResponse result = placeService.searchPlaces(LoginSession.currentUserId(request), filter);
        model.addAttribute("places", result.getPlaces());
        model.addAttribute("filtered", result.isFiltered());
        model.addAttribute("regionLabel", result.getRegionLabel());
        model.addAttribute("dateLabel", result.getDateLabel());
        model.addAttribute("timeLabel", result.getTimeLabel());
        model.addAttribute("petLabel", result.getPetLabel());
        return "nearby";
    }

    /**
     * 검증·바인딩 실패를 화면에 그대로 쓸 수 있는 한국어 문구로 바꾼다.
     *
     * <p>{@code @AssertTrue} 는 우리가 적어 둔 문구를 그대로 갖고 있지만, 날짜 문자열이나
     * 장소 유형처럼 <b>타입 변환</b>이 깨진 경우의 기본 문구는 영어다. 그건 필드 이름으로 바꿔 적는다.
     */
    private static List<String> filterErrors(BindingResult bindingResult) {
        return bindingResult.getAllErrors().stream()
                .map(HomeController::filterErrorMessage)
                .distinct()
                .toList();
    }

    private static String filterErrorMessage(ObjectError error) {
        if (error instanceof FieldError fieldError && fieldError.isBindingFailure()) {
            // petIds[1] 처럼 목록 항목이면 색인을 떼고 필드 이름만 남긴다.
            String path = fieldError.getField();
            int index = path.indexOf('[');
            String field = index < 0 ? path : path.substring(0, index);
            return FILTER_FIELD_LABELS.getOrDefault(field, "검색 조건") + " 값을 다시 선택해 주세요";
        }
        String message = error.getDefaultMessage();
        return message == null || message.isBlank()
                ? "검색 조건을 다시 확인해 주세요"
                : message;
    }

}
