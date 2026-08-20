package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.service.HostService;
import com.example.petnow.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HostController.class)
class HostDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HostService hostService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("장소 카드가 게시글 상세로 가는 링크를 갖는다")
    void placeCardLinksToPlaceDetail() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of(place()));

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("host/dashboard"))
                .andExpect(content().string(containsString("class=\"place-summary\" href=\"/places/3\"")))
                .andExpect(content().string(containsString("게시글 보기")));
    }

    @Test
    @DisplayName("카드에 일정 관리, 리뷰 관리와 비활성 삭제 버튼이 표시된다")
    void placeCardShowsPrototypeActions() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of(place()));

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("장소 정보 수정")))
                .andExpect(content().string(containsString("/places/edit/3")))
                .andExpect(content().string(containsString("예약 가능 시간 관리")))
                .andExpect(content().string(containsString("/host/places/3/availability")))
                .andExpect(content().string(containsString("리뷰 관리")))
                .andExpect(content().string(containsString("/reviews/place/3")))
                .andExpect(content().string(containsString("btn btn-danger btn-icon\" type=\"button\" disabled")))
                .andExpect(content().string(not(containsString("공개 화면 보기"))));
    }

    @Test
    @DisplayName("탭 파라미터가 없으면 내 호스팅 탭을 연다")
    void defaultsToPlacesTab() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of(place()));

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("햇살 가득한 마당")));
    }

    @Test
    @DisplayName("예약 요청 탭은 호스트용 API가 없다는 것을 안내한다")
    void bookingTabShowsPendingNotice() throws Exception {
        mockMvc.perform(get("/host").param("tab", "booking").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("예약 요청을 불러올 수 없어요")))
                .andExpect(content().string(not(containsString("새 장소 등록하기"))));
    }

    private HostPlaceListResponse place() {
        return HostPlaceListResponse.builder()
                .id(3L)
                .name("햇살 가득한 마당")
                .status(PlaceStatus.PUBLISHED)
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
