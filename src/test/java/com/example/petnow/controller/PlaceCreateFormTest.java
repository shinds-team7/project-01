package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PlaceUpdateRequest;
import com.example.petnow.dto.response.PlacePhotoResponse;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
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
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

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
                .andExpect(model().attributeHasFieldErrors("placeForm", "sigungu"));

        then(placeService).should(never()).createPlace(any(), any());
    }

    @Test
    @DisplayName("도로명 주소를 입력하지 않으면 장소를 등록하지 않고 폼을 다시 보여준다")
    void rejectsCreateWithoutRoadAddress() throws Exception {
        mockMvc.perform(validPlaceRequest()
                        .param("sigungu", "성동구"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/create"))
                .andExpect(model().attributeHasFieldErrors("placeForm", "roadAddress"));

        then(placeService).should(never()).createPlace(any(), any());
    }

    @Test
    @DisplayName("장소 수정 폼은 기존 값을 채우고 수정 주소로 제출한다")
    void editFormProvidesExistingValues() throws Exception {
        PlaceUpdateRequest request = validUpdateRequest();
        given(placeService.getUpdateForm(1L, 3L)).willReturn(request);
        given(placeService.getPlacePhotosForEdit(1L, 3L)).willReturn(List.of(
                PlacePhotoResponse.builder().id(9L).imageUrl("/uploads/places/yard.jpg").sortOrder(0).build()));

        mockMvc.perform(get("/places/edit/3").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("places/create"))
                .andExpect(content().string(containsString("게시글 수정")))
                .andExpect(content().string(containsString("action=\"/places/edit/3\"")))
                .andExpect(content().string(containsString("/uploads/places/yard.jpg")))
                .andExpect(content().string(containsString("/places/edit/3/photos/9/delete")))
                .andExpect(content().string(containsString("value=\"햇살 가득한 마당\"")));
    }

    @Test
    @DisplayName("장소 수정 성공 후 공개 상세로 이동한다")
    void updatesPlaceAndRedirectsToDetail() throws Exception {
        mockMvc.perform(post("/places/edit/3").session(loggedIn())
                        .param("name", "햇살 가득한 마당")
                        .param("description", "수정한 소개")
                        .param("sigungu", "성동구")
                        .param("roadAddress", "서울특별시 성동구 왕십리로 1")
                        .param("placeType", "HOUSE")
                        .param("areaSize", "42")
                        .param("capacity", "2")
                        .param("hourlyPrice", "12000")
                        .param("nightlyPrice", "48000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/places/3"));

        then(placeService).should().updatePlace(eq(1L), eq(3L), any(PlaceUpdateRequest.class));
    }

    @Test
    @DisplayName("장소 수정 화면에서 사진 한 장을 삭제하고 수정 화면으로 돌아간다")
    void deletesPhotoAndRedirectsToEdit() throws Exception {
        mockMvc.perform(post("/places/edit/3/photos/9/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/places/edit/3"));

        then(placeService).should().deletePlacePhoto(1L, 3L, 9L);
    }

    @Test
    @DisplayName("장소를 삭제하면 호스트 대시보드 내 호스팅 탭으로 돌아간다")
    void deletesPlaceAndRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/places/3/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host?tab=places"));

        then(placeService).should().deletePlace(1L, 3L);
    }

    @Test
    @DisplayName("대기·확정 예약이 있으면 삭제하지 않고 오류 메시지와 함께 되돌아간다")
    void rejectsDeleteWhenPlaceHasActiveReservations() throws Exception {
        org.mockito.BDDMockito.willThrow(new BusinessException(PlaceErrorCode.PLACE_HAS_ACTIVE_RESERVATIONS))
                .given(placeService).deletePlace(1L, 3L);

        mockMvc.perform(post("/places/3/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host?tab=places"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attributeExists("placeDeleteError"));
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

    private PlaceUpdateRequest validUpdateRequest() {
        PlaceUpdateRequest request = new PlaceUpdateRequest();
        request.setName("햇살 가득한 마당");
        request.setDescription("반려견이 편하게 쉴 수 있는 공간입니다.");
        request.setSigungu("성동구");
        request.setRoadAddress("서울특별시 성동구 왕십리로 1");
        request.setPlaceType(PlaceType.HOUSE);
        request.setAreaSize(new BigDecimal("42"));
        request.setCapacity(2);
        request.setHourlyPrice(new BigDecimal("12000"));
        request.setNightlyPrice(new BigDecimal("48000"));
        return request;
    }
}
