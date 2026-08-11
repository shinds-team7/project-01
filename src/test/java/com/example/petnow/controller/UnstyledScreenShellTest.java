package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.HostService;
import com.example.petnow.service.MyProfileServiceImpl;
import com.example.petnow.service.ReservationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 스타일시트가 아예 링크되지 않았던 화면들이 앱 셸로 그려지는지 확인한다.
 *
 * <p>예약 목록·예약 상세·내 호스팅 장소·내 정보는 CSS 링크 없이 맨 HTML 이거나
 * 화면별 인라인 style 이라 프로토타입 디자인 시스템 밖에 있었다.
 */
@WebMvcTest(controllers = {ReservationController.class, HostController.class, MyProfileController.class})
class UnstyledScreenShellTest {

    @MockitoBean
    private ReservationService reservationService;

    /** ReservationController 가 예약 폼용으로 직접 들고 있는 의존성이라 슬라이스에 필요하다. */
    @MockitoBean
    private PlaceMapper placeMapper;

    @MockitoBean
    private HostService hostService;

    @MockitoBean
    private MyProfileServiceImpl myProfileService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("예약 목록이 앱 셸로 그려지고 상태를 배지로 보여준다")
    void reservationListRendersWithAppShell() throws Exception {
        given(reservationService.getReservationList(eq(1L), any())).willReturn(List.of(listItem()));

        mockMvc.perform(get("/reservation/list").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/reservationList"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("연남동 윤슬 호스트")))
                .andExpect(content().string(containsString("예약 확정")));
    }

    @Test
    @DisplayName("예약이 없으면 앱 셸의 빈 상태를 그린다")
    void reservationListRendersEmptyState() throws Exception {
        given(reservationService.getReservationList(eq(1L), any())).willReturn(List.of());

        mockMvc.perform(get("/reservation/list").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("조회된 예약 내역이 없어요")));
    }

    @Test
    @DisplayName("예약 상세가 앱 셸로 그려지고 동반 반려동물을 함께 보여준다")
    void reservationDetailRendersWithAppShell() throws Exception {
        given(reservationService.detailReservation(9L, 1L)).willReturn(detail());

        mockMvc.perform(get("/reservation/detail").param("reservationId", "9").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/reservationDetail"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("PN-20260805-0001")))
                .andExpect(content().string(containsString("초코")));
    }

    /** #179 로 host/manage.html 이 host/dashboard.html 로 교체됐다. */
    @Test
    @DisplayName("내 호스팅 장소가 앱 셸로 그려지고 게시 상태를 배지로 보여준다")
    void hostDashboardRendersWithAppShell() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of(
                HostPlaceListResponse.builder().id(3L).name("햇살 가득한 마당").status(PlaceStatus.PUBLISHED).build()));

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("host/dashboard"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("게시")));
    }

    @Test
    @DisplayName("장소가 없으면 빈 상태 안내를 그린다")
    void hostDashboardRendersEmptyState() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of());

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("등록된 장소가 없어요")))
                // 빈 상태 카드에는 버튼을 두지 않는다. 등록 입구는 상단 "+ 장소 등록" 과
                // 하단 "새 장소 등록하기" 두 곳뿐이고, 이건 프로토타입 HOST HOME 그대로다.
                .andExpect(content().string(containsString("새 장소 등록하기")));
    }

    @Test
    @DisplayName("내 정보가 앱 셸로 그려진다 - 이전에는 뷰 이름 오타로 화면 자체가 열리지 않았다")
    void myProfileRendersWithAppShell() throws Exception {
        given(myProfileService.getProfile(1L)).willReturn(MyProfileResponse.builder()
                .nickname("지우").email("jiwoo@petnow.kr").phone("010-1234-5678").build());

        mockMvc.perform(get("/my/profile").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/profile"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("010-1234-5678")));
    }

    private ReservationListResponse listItem() {
        return new ReservationListResponse(
                9L, "연남동 윤슬 호스트", new BigDecimal("48000"), ReservationStatus.CONFIRMED,
                LocalDateTime.of(2026, 8, 5, 14, 0), LocalDateTime.of(2026, 8, 5, 20, 0));
    }

    private ReservationDetailResponse detail() {
        ReservationDetailResponse.PetDetail pet = new ReservationDetailResponse.PetDetail();
        pet.setPetId(7L);
        pet.setPetName("초코");
        pet.setWeight(4.2);
        pet.setSize("소형");

        return new ReservationDetailResponse(
                9L, "PN-20260805-0001", ReservationStatus.CONFIRMED,
                LocalDateTime.of(2026, 8, 5, 14, 0), LocalDateTime.of(2026, 8, 5, 20, 0),
                "지우", "연남동 윤슬 호스트", List.of(pet));
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
