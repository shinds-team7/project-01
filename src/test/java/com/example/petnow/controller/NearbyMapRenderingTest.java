package com.example.petnow.controller;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 내 주변 지도(#277) 중 화면이 책임지는 부분을 고정한다.
 *
 * <p>지도 자체는 브라우저에서 그려지므로 여기서 검증할 수 있는 건 "서버가 무엇을 내려보내는가"다.
 * 다만 그 무엇이 사고가 나는 지점이다.
 * <ol>
 *   <li>JavaScript 키는 내려가야 하고, REST API 키는 어떤 경우에도 내려가면 안 된다.</li>
 *   <li>좌표가 없는 장소도 목록에는 남아야 한다. 지도에 못 찍는 것과 목록에서 사라지는 건 다르다.</li>
 * </ol>
 */
@WebMvcTest(value = HomeController.class, properties = {
        "app.kakao.map.javascript-key=" + NearbyMapRenderingTest.JAVASCRIPT_KEY,
        "app.kakao.rest-api-key=" + NearbyMapRenderingTest.REST_API_KEY
})
class NearbyMapRenderingTest {

    static final String JAVASCRIPT_KEY = "browserkey0000000000000000000001";
    /** 이 값이 응답에 한 글자라도 섞이면 서버 키가 유출된 것이다. */
    static final String REST_API_KEY = "serverkey0000000000000000000002";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @Test
    @DisplayName("내 주변이 카카오맵 SDK 를 JavaScript 키로 불러온다")
    void loadsKakaoSdkWithJavascriptKey() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(List.of(withCoordinates()));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dapi.kakao.com/v2/maps/sdk.js")))
                .andExpect(content().string(containsString("appkey=" + JAVASCRIPT_KEY)))
                // 지도가 붙었으니 준비 중 안내는 사라져야 한다
                .andExpect(content().string(not(containsString("거리순 정렬과 지도 표시는 준비 중"))));
    }

    @Test
    @DisplayName("서버 전용 REST API 키는 화면에 실리지 않는다")
    void neverLeaksRestApiKey() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(List.of(withCoordinates()));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(REST_API_KEY))));
    }

    @Test
    @DisplayName("SDK 는 내 주변에서만 불러온다")
    void doesNotLoadKakaoSdkOnOtherScreens() throws Exception {
        // 전역 레이아웃에 넣으면 지도를 쓰지 않는 화면까지 SDK 를 내려받는다.
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("dapi.kakao.com"))))
                .andExpect(content().string(not(containsString(JAVASCRIPT_KEY))));
    }

    @Test
    @DisplayName("좌표가 있는 장소만 카드에 좌표가 붙고, 좌표가 없는 장소도 목록에는 남는다")
    void placesWithoutCoordinatesStayInTheList() throws Exception {
        given(placeService.getPublishedPlaces())
                .willReturn(List.of(withCoordinates(), withoutCoordinates()));

        String html = mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                // 좌표가 없어도 목록에서 빠지지 않는다
                .andExpect(content().string(containsString("좌표 없는 공간")))
                .andExpect(content().string(containsString("좌표 있는 공간")))
                .andExpect(content().string(containsString("data-lat=\"37.5665\"")))
                .andExpect(content().string(containsString("data-lng=\"126.978\"")))
                .andReturn().getResponse().getContentAsString();

        // 좌표가 null 이면 Thymeleaf 가 속성 자체를 그리지 않는다. 두 장 중 한 장에만 붙어야 한다.
        assertThat(countOf(html, "data-lat=")).isOne();
        assertThat(countOf(html, "data-place-id=")).isEqualTo(2);
    }

    private static int countOf(String text, String token) {
        int count = 0;
        for (int at = text.indexOf(token); at >= 0; at = text.indexOf(token, at + token.length())) {
            count++;
        }
        return count;
    }

    private PlaceListResponse withCoordinates() {
        return place(1L, "좌표 있는 공간")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.978"))
                .build();
    }

    private PlaceListResponse withoutCoordinates() {
        return place(2L, "좌표 없는 공간").build();
    }

    private PlaceListResponse.PlaceListResponseBuilder place(long id, String name) {
        return PlaceListResponse.builder()
                .id(id)
                .name(name)
                .nickname("윤슬")
                .placeType(PlaceType.APARTMENT)
                .hourlyPrice(BigDecimal.valueOf(5000));
    }
}
