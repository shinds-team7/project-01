package com.example.petnow.controller;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 홈과 하단 네비 목적지가 프로토타입 앱 셸로 그려지는지 확인한다.
 *
 * <p>셸이 빠지면 화면이 스타일 없는 맨 HTML 로 나오므로 app.css 링크 유무로 잡는다.
 */
@WebMvcTest(value = HomeController.class, properties = "app.public-url=https://petnow.example")
class HomeShellRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("홈이 구형 데스크톱 레이아웃과 로그인·회원가입 입구를 갖고 그려진다")
    void homeRendersWithClassicHomeLayout() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("/css/home.css")))
                .andExpect(content().string(containsString("class=\"site-header\"")))
                .andExpect(content().string(containsString("/css/home-classic.css")))
                .andExpect(content().string(containsString("/auth/login")))
                .andExpect(content().string(containsString("편안한 하루</em>를 맡겨요")));
    }

    @Test
    @DisplayName("홈이 카카오톡 링크 미리보기용 Open Graph 정보를 절대 URL로 제공한다")
    void homeRendersOpenGraphMetadata() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "property=\"og:image\" content=\"https://petnow.example/images/paw.png\"")))
                .andExpect(content().string(containsString(
                        "property=\"og:url\" content=\"https://petnow.example/home\"")))
                .andExpect(content().string(containsString(
                        "property=\"og:title\" content=\"홈 | Pet NOW\"")));
    }

    @Test
    @DisplayName("조회할 장소가 없으면 홈이 500 이 아니라 빈 상태 안내를 그린다")
    void homeRendersEmptyRecentPlaces() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아직 둘러볼 수 있는 호스트가 없어요")));
    }

    /**
     * 프로토타입 카드는 PlaceListResponse 에 없는 address·priceLabel 을 참조했다.
     * 지금 쓰는 placeType·hourlyPrice 로 실제로 그려지는지 항목이 있을 때만 확인할 수 있다.
     */
    @Test
    @DisplayName("장소가 있으면 카드에 이름·유형·시간당 가격이 그려진다")
    void homeRendersRecentPlaceCard() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(List.of(place(3L, "햇살 가득한 마당", 5000)));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("아파트")))
                // #numbers.formatInteger 로 천 단위 구분이 들어가야 한다
                .andExpect(content().string(containsString("5,000원~")))
                .andExpect(content().string(containsString("/places/3")))
                // 항목이 있으면 빈 상태 안내는 나오지 않는다
                .andExpect(content().string(not(containsString("아직 둘러볼 수 있는 호스트가 없어요"))));
    }

    @Test
    @DisplayName("공개 장소가 많아도 홈에는 정해진 개수만 넘긴다")
    void homeLimitsRecentPlaces() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(
                IntStream.rangeClosed(1, 20)
                        .mapToObj(i -> place(i, "장소 " + i, 5000))
                        .toList());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("recentPlaces", org.hamcrest.Matchers.hasSize(8)))
                .andExpect(content().string(containsString("장소 8")))
                .andExpect(content().string(not(containsString("장소 9"))));
    }

    private PlaceListResponse place(long id, String name, int hourlyPrice) {
        return PlaceListResponse.builder()
                .id(id)
                .name(name)
                .nickname("윤슬")
                .placeType(PlaceType.APARTMENT)
                .hourlyPrice(BigDecimal.valueOf(hourlyPrice))
                .build();
    }

    @Test
    @DisplayName("검색은 준비 화면 대신 장소 목록으로 연결된다")
    void navDestinationsResolve() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/js/home.js")));
    }

    @Test
    @DisplayName("내 주변이 준비 중 화면 대신 실제 목록을 그린다")
    void nearbyRendersPlaceList() throws Exception {
        given(placeService.searchPlaces(org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(PlaceFilterRequest.class)))
                .willReturn(searchResult(List.of(place(3L, "햇살 가득한 마당", 5000))));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(view().name("nearby"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("class=\"is-type\">아파트")))
                .andExpect(content().string(containsString("/places/3")))
                // 지도가 붙었으므로(#277) 준비 중 안내는 화면에서 사라졌다
                .andExpect(content().string(not(containsString("거리순 정렬과 지도 표시는 준비 중"))));
    }

    @Test
    @DisplayName("공개된 장소가 없으면 내 주변도 빈 상태를 그린다")
    void nearbyRendersEmptyState() throws Exception {
        given(placeService.searchPlaces(org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(PlaceFilterRequest.class)))
                .willReturn(searchResult(List.of()));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("주변에 공개된 공간이 없어요")));
    }

    /** 조건 없이 들어온 검색 결과. 라벨이 모두 비어 있어 필터 칩도 그려지지 않는다. */
    private PlaceSearchResponse searchResult(List<PlaceListResponse> places) {
        return PlaceSearchResponse.builder().places(places).build();
    }
}
