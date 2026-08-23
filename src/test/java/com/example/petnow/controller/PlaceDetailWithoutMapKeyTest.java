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

@WebMvcTest(value = PlaceController.class, properties = "kakao.map.javascript-key=")
class PlaceDetailWithoutMapKeyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @Test
    void doesNotLoadSdkWhenJavascriptKeyIsBlank() throws Exception {
        PlaceDetailResponse place = new PlaceDetailResponse();
        place.setId(1L);
        place.setName("지도 테스트 장소");
        place.setLatitude(new BigDecimal("37.5665"));
        place.setLongitude(new BigDecimal("126.978"));
        given(placeService.getPlaceDetail(1L, null)).willReturn(place);

        mockMvc.perform(get("/places/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("dapi.kakao.com"))))
                .andExpect(content().string(containsString("data-lat=\"37.5665\"")));
    }
}
