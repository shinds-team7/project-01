package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PlaceController.class)
class PlaceCreateFormTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @Test
    @DisplayName("장소 등록 폼은 서울 25개 지역구와 도로명 주소 입력란을 제공한다")
    void createFormProvidesAddressFields() throws Exception {
        String html = mockMvc.perform(get("/places/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("name=\"roadAddress\"");
        assertThat(Pattern.compile("<option value=\"[^\"]+\">").matcher(html).results())
                .hasSize(25);
    }

    @Test
    @DisplayName("지역구를 입력하지 않으면 장소를 등록하지 않고 폼을 다시 보여준다")
    void rejectsCreateWithoutSigungu() throws Exception {
        mockMvc.perform(validPlaceRequest()
                        .param("roadAddress", "서울특별시 성동구 왕십리로 83-21"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/create"))
                .andExpect(model().attributeHasFieldErrors("placeCreateRequest", "sigungu"));

        then(placeService).should(never()).createPlace(any(), any());
    }

    @Test
    @DisplayName("도로명 주소를 입력하지 않으면 장소를 등록하지 않고 폼을 다시 보여준다")
    void rejectsCreateWithoutRoadAddress() throws Exception {
        mockMvc.perform(validPlaceRequest()
                        .param("sigungu", "성동구"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/create"))
                .andExpect(model().attributeHasFieldErrors("placeCreateRequest", "roadAddress"));

        then(placeService).should(never()).createPlace(any(), any());
    }

    private MockHttpServletRequestBuilder validPlaceRequest() {
        return post("/places/create")
                .session(loggedIn())
                .param("name", "성수 조용한 단독주택")
                .param("description", "반려견이 편하게 쉴 수 있는 공간입니다.")
                .param("placeType", "HOUSE")
                .param("areaSize", "42")
                .param("capacity", "2")
                .param("hourlyPrice", "12000")
                .param("nightlyPrice", "48000");
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
