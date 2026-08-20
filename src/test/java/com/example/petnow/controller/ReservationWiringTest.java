package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.PetService;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationWiringTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private PlaceMapper placeMapper;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("실제 예약 상세가 취소 확인 모달을 예약 취소 엔드포인트에 연결한다")
    void reservationDetailWiresCancelAction() throws Exception {
        given(reservationService.detailReservation(9L, 1L)).willReturn(detail());

        mockMvc.perform(get("/reservation/detail")
                        .param("reservationId", "9")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-controls=\"reservation-cancel-modal\"")))
                .andExpect(content().string(containsString("action=\"/reservation/cancel\"")))
                .andExpect(content().string(containsString("name=\"reservationId\" value=\"9\"")))
                .andExpect(content().string(containsString("/js/modal.js")))
                .andExpect(content().string(containsString("예약을 취소할까요?")));
    }

    @Test
    @DisplayName("예약 취소 요청이 로그인 사용자와 예약 ID로 서비스에 전달된다")
    void cancelActionReachesService() throws Exception {
        mockMvc.perform(post("/reservation/cancel")
                        .param("reservationId", "9")
                        .session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservation/list"));

        then(reservationService).should().cancelReservation(9L, 1L);
    }

    private ReservationDetailResponse detail() {
        ReservationDetailResponse response = new ReservationDetailResponse();
        response.setReservationId(9L);
        response.setReservationNo("PN-20260805-0001");
        response.setStatus(ReservationStatus.CONFIRMED);
        response.setPlaceName("연남동 윤슬 호스트");
        response.setPets(List.of());
        return response;
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
