package com.example.petnow.controller;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PlaceController.class, properties = {
        "kakao.map.javascript-key=" + PlaceDetailMapRenderingTest.JAVASCRIPT_KEY,
        "kakao.local-api.rest-api-key=" + PlaceDetailMapRenderingTest.REST_API_KEY
})
class PlaceDetailMapRenderingTest {

    static final String JAVASCRIPT_KEY = "browserkey0000000000000000000001";
    static final String REST_API_KEY = "serverkey0000000000000000000002";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @Test
    void rendersKakaoMapForPlaceWithCoordinates() throws Exception {
        given(placeService.getPlaceDetail(1L, null)).willReturn(placeWithCoordinates());

        mockMvc.perform(get("/places/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dapi.kakao.com/v2/maps/sdk.js")))
                .andExpect(content().string(containsString("appkey=" + JAVASCRIPT_KEY)))
                .andExpect(content().string(containsString("data-lat=\"37.5665\"")))
                .andExpect(content().string(containsString("data-lng=\"126.978\"")))
                .andExpect(content().string(not(containsString(REST_API_KEY))));
    }

    @Test
    void rendersLocationHintWithoutCoordinatesAndDoesNotLoadSdk() throws Exception {
        given(placeService.getPlaceDetail(1L, null)).willReturn(placeWithoutCoordinates());

        mockMvc.perform(get("/places/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("dapi.kakao.com"))))
                .andExpect(content().string(containsString("아직 지도에 표시할 위치 정보가 준비되지 않았어요.")));
    }

    private PlaceDetailResponse placeWithCoordinates() {
        PlaceDetailResponse place = placeWithoutCoordinates();
        place.setLatitude(new BigDecimal("37.5665"));
        place.setLongitude(new BigDecimal("126.978"));
        return place;
    }

    private PlaceDetailResponse placeWithoutCoordinates() {
        PlaceDetailResponse place = new PlaceDetailResponse();
        place.setId(1L);
        place.setName("지도 테스트 장소");
        return place;
    }
}
