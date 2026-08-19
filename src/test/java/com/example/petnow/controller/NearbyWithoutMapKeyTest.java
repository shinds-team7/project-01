package com.example.petnow.controller;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.entity.PlaceType;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 지도 키가 없는 환경에서도 내 주변이 멀쩡히 열리는지 본다. (#277)
 *
 * <p>키는 환경변수로 주입한다. 새로 클론한 팀원의 로컬, 키를 넣지 않은 CI, 키를 잘못 지운
 * 배포까지 전부 키가 빈 상태다. 그때 화면이 깨지거나 500 이 나면 지도 하나 때문에
 * 목록 기능까지 못 쓰게 된다.
 */
@WebMvcTest(value = HomeController.class, properties = "kakao.map.javascript-key=")
class NearbyWithoutMapKeyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("지도 키가 없으면 SDK 를 부르지 않고 목록만 그린다")
    void rendersListWithoutMapSdk() throws Exception {
        given(placeService.searchPlaces(isNull(), any(PlaceFilterRequest.class)))
                .willReturn(searchResult(List.of(PlaceListResponse.builder()
                        .id(9L)
                        .name("햇살 가득한 마당")
                        .nickname("윤슬")
                        .placeType(PlaceType.APARTMENT)
                        .hourlyPrice(BigDecimal.valueOf(5000))
                        .build())));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                // 키 없이 SDK 를 부르면 401 만 찍힌다. 아예 부르지 않는다.
                .andExpect(content().string(not(containsString("dapi.kakao.com"))))
                // 지도가 없어도 목록은 그대로다
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("/places/9")));
    }

    @Test
    @DisplayName("지도 키가 없어도 빈 목록은 빈 상태 안내를 그린다")
    void rendersEmptyStateWithoutMapSdk() throws Exception {
        given(placeService.searchPlaces(isNull(), any(PlaceFilterRequest.class)))
                .willReturn(searchResult(List.of()));

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("주변에 공개된 공간이 없어요")));
    }

    /** 조건 없이 들어온 검색 결과. */
    private PlaceSearchResponse searchResult(List<PlaceListResponse> places) {
        return PlaceSearchResponse.builder().places(places).build();
    }
}
