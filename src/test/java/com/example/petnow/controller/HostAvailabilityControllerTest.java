package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.entity.Place;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.PlaceAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HostAvailabilityController.class)
class HostAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceAvailabilityService availabilityService;

    @MockitoBean
    private PlaceMapper placeMapper;

    @Test
    void rendersSeparateAvailabilityScreenForOwner() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 20);
        given(placeMapper.findById(1L)).willReturn(ownedPlace());
        given(availabilityService.getHostSlots(7L, 1L, date)).willReturn(List.of());
        given(availabilityService.getSlotPeriod(7L, 1L)).willReturn(
                new PlaceSlotPeriodResponse(
                        LocalDate.of(2026, 8, 18),
                        LocalDate.of(2026, 8, 24)));

        mockMvc.perform(get("/host/places/1/availability")
                        .param("date", date.toString())
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("예약 가능 시간 관리")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("예약 운영 정책")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("패키지 예약 지원")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"fromDate\" name=\"fromDate\" value=\"2026-08-18\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"toDate\" name=\"toDate\" value=\"2026-08-24\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("하루 8개의 3시간 슬롯")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("설정 완료")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/host?tab=places\"")));
    }

    @Test
    void createsSlotsAndRedirectsWithPostRedirectGet() throws Exception {
        LocalDate from = LocalDate.of(2026, 8, 20);
        LocalDate to = LocalDate.of(2026, 8, 26);

        mockMvc.perform(post("/host/places/1/availability")
                        .param("fromDate", from.toString())
                        .param("toDate", to.toString())
                        .session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host/places/1/availability?date=2026-08-20"));

        verify(availabilityService).createSlots(7L, 1L, from, to);
    }

    private Place ownedPlace() {
        return Place.builder().id(1L).hostUserId(7L).name("테스트 장소")
                .supportsHourly(true).build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 7L);
        return session;
    }
}
