package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReviewReplyRequest;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.HostService;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HostController.class)
class HostReviewReplyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HostService hostService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private PlaceMapper placeMapper;

    @Test
    @DisplayName("답글 등록은 서비스로 전달되고 장소 리뷰 화면으로 돌아간다")
    void savesReplyAndRedirects() throws Exception {
        mockMvc.perform(post("/host/places/3/reviews/11/reply").session(loggedIn())
                        .param("content", "감사합니다, 다음에도 편하게 맡겨주세요."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host/places/3/reviews"));

        then(reviewService).should().saveReply(
                org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(11L), any(ReviewReplyRequest.class));
    }

    @Test
    @DisplayName("빈 답글은 서비스를 건드리지 않고 오류 메시지와 함께 되돌아간다")
    void blankReplyIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/host/places/3/reviews/11/reply").session(loggedIn())
                        .param("content", "  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host/places/3/reviews"))
                .andExpect(flash().attributeExists("replyError"));

        then(reviewService).should(never()).saveReply(any(), any(), any(), any());
    }

    @Test
    @DisplayName("답글 삭제 요청이 로그인 사용자·장소·리뷰 id 와 함께 서비스에 전달된다")
    void deletesReplyAndRedirects() throws Exception {
        mockMvc.perform(post("/host/places/3/reviews/11/reply/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host/places/3/reviews"));

        then(reviewService).should().deleteReply(1L, 3L, 11L);
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
