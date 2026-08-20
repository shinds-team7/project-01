package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.Pet;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Test
    @DisplayName("예약 목록이 이용상태 필터와 이용상태 배지를 함께 그린다")
    void reservationListWiresUseStatusFilter() throws Exception {
        given(reservationService.getReservationList(1L, "BEFORE_USE"))
                .willReturn(List.of(new ReservationListResponse(
                        9L,
                        "연남동 윤슬 호스트",
                        new BigDecimal("48000"),
                        ReservationStatus.CONFIRMED,
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(2).plusHours(6))));

        mockMvc.perform(get("/reservation/list")
                        .param("useStatus", "BEFORE_USE")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/reservation/list?useStatus=BEFORE_USE")))
                .andExpect(content().string(containsString("/reservation/list?useStatus=IN_USE")))
                .andExpect(content().string(containsString("/reservation/list?useStatus=AFTER_USE")))
                .andExpect(content().string(containsString("예약 이용상태 필터")))
                .andExpect(content().string(containsString("badge-use-before")))
                .andExpect(content().string(containsString("이용 전")));

        then(reservationService).should().getReservationList(1L, "BEFORE_USE");
    }

    @Test
    @DisplayName("예약 상세가 현재 선택을 유지한 반려동물 변경 폼을 그린다")
    void reservationDetailWiresPetChangeForm() throws Exception {
        ReservationDetailResponse detail = detail();
        ReservationDetailResponse.PetDetail selectedPet = new ReservationDetailResponse.PetDetail();
        selectedPet.setPetId(7L);
        selectedPet.setPetName("초코");
        detail.setPets(List.of(selectedPet));

        given(reservationService.detailReservation(9L, 1L)).willReturn(detail);
        given(petService.getPetList(1L)).willReturn(List.of(
                pet(7L, "초코"),
                pet(8L, "보리")));

        mockMvc.perform(get("/reservation/detail")
                        .param("reservationId", "9")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-controls=\"reservation-pets-modal\"")))
                .andExpect(content().string(containsString("action=\"/reservation/9/edit\"")))
                .andExpect(content().string(containsString("name=\"petIds\" value=\"7\" checked")))
                .andExpect(content().string(containsString("name=\"petIds\" value=\"8\"")))
                .andExpect(content().string(containsString("동반 반려동물 변경")));
    }

    @Test
    @DisplayName("동반 반려동물 변경 요청이 선택 ID와 로그인 사용자로 서비스에 전달된다")
    void petChangeActionReachesService() throws Exception {
        mockMvc.perform(post("/reservation/9/edit")
                        .param("petIds", "7", "8")
                        .session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservation/detail?reservationId=9"));

        then(reservationService).should().changeReservationPet(9L, List.of(7L, 8L), 1L);
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

    private PetListResponse pet(Long id, String name) {
        return PetListResponse.builder()
                .id(id)
                .name(name)
                .size(Pet.Size.SMALL)
                .weight(4.5)
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
