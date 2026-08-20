package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.ReviewResponse;
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
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewWiringTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("내 리뷰 목록이 수정 링크와 삭제 확인 모달을 실제 엔드포인트에 연결한다")
    void myReviewActionsAreWired() throws Exception {
        ReviewResponse review = mock(ReviewResponse.class);
        given(review.getId()).willReturn(11L);
        given(review.getPlaceId()).willReturn(1L);
        given(review.getPlaceName()).willReturn("성수 조용한 단독주택 마당");
        given(review.getRating()).willReturn(5);
        given(review.getContent()).willReturn("마당이 넓어서 좋았어요");
        given(reviewService.getMyReviews(1L)).willReturn(List.of(review));

        mockMvc.perform(get("/reviews/my").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/reviews/11/edit\"")))
                .andExpect(content().string(containsString("aria-controls=\"review-delete-11\"")))
                .andExpect(content().string(containsString("action=\"/reviews/11/delete\"")))
                .andExpect(content().string(containsString("/js/modal.js")))
                .andExpect(content().string(containsString("삭제한 리뷰는 복구할 수 없어요")))
                .andExpect(content().string(not(containsString("준비 중이에요"))));
    }

    @Test
    @DisplayName("리뷰 수정·삭제 요청이 로그인 사용자 정보와 함께 서비스에 전달된다")
    void reviewActionsReachService() throws Exception {
        mockMvc.perform(post("/reviews/11").session(loggedIn())
                        .param("rating", "4")
                        .param("content", "수정한 리뷰예요"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/my"));

        then(reviewService).should().updateReview(eq(1L), eq(11L), any());

        mockMvc.perform(post("/reviews/11/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/my"));

        then(reviewService).should().deleteReview(1L, 11L);
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
