package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.ReviewSortType;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private PlaceMapper placeMapper;

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
                .andExpect(content().string(containsString("/host/places/3/reviews")))
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

    @Test
    @DisplayName("리뷰 탭은 내 장소 전체의 리뷰를 모아 보여준다")
    void reviewsTabListsReviewsAcrossMyPlaces() throws Exception {
        // review() 안에서 목을 세우므로 given(...) 을 열기 전에 미리 만들어 둔다.
        // given(...).willReturn(List.of(review())) 로 쓰면 스터빙이 겹쳐 UnfinishedStubbingException 이 난다.
        ReviewResponse review = review();
        given(reviewService.getReviewsForHost(1L)).willReturn(List.of(review));

        mockMvc.perform(get("/host").param("tab", "reviews").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("host/dashboard"))
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("마당이 넓어서 좋았어요")))
                // 장소명은 그 장소 하나만 보는 호스트 리뷰 화면으로 간다.
                .andExpect(content().string(containsString("href=\"/host/places/3/reviews\"")))
                .andExpect(content().string(not(containsString("아직 등록된 리뷰가 없어요"))));
    }

    @Test
    @DisplayName("리뷰가 없으면 리뷰 탭은 빈 상태 안내를 보여준다")
    void reviewsTabShowsEmptyState() throws Exception {
        given(reviewService.getReviewsForHost(1L)).willReturn(List.of());

        mockMvc.perform(get("/host").param("tab", "reviews").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아직 등록된 리뷰가 없어요")));
    }

    @Test
    @DisplayName("장소별 리뷰 관리 화면은 장소 주인에게 리뷰와 평균 별점을 보여준다")
    void placeReviewsRendersForOwner() throws Exception {
        ReviewResponse review = review();
        given(placeMapper.findById(3L)).willReturn(ownedPlace());
        given(reviewService.getReviewsByPlace(3L, ReviewSortType.LATEST)).willReturn(List.of(review));

        mockMvc.perform(get("/host/places/3/reviews").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("host/reviews"))
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("마당이 넓어서 좋았어요")))
                // 평균은 목록에서 화면이 직접 낸다. 별 하나짜리 리뷰 한 건이면 5.00 이 아니라 5.00.
                .andExpect(content().string(containsString("리뷰 1개")))
                .andExpect(content().string(containsString("2026.07.18 이용")));
    }

    @Test
    @DisplayName("남의 장소 리뷰는 URL 을 알아도 열리지 않는다")
    void placeReviewsRejectsNonOwner() throws Exception {
        Place someoneElses = ownedPlace();
        someoneElses.setHostUserId(99L);
        given(placeMapper.findById(3L)).willReturn(someoneElses);

        mockMvc.perform(get("/host/places/3/reviews").session(loggedIn()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("없는 장소의 리뷰 관리 화면은 404 다")
    void placeReviewsRejectsMissingPlace() throws Exception {
        given(placeMapper.findById(3L)).willReturn(null);

        mockMvc.perform(get("/host/places/3/reviews").session(loggedIn()))
                .andExpect(status().isNotFound());
    }

    private Place ownedPlace() {
        return Place.builder()
                .id(3L)
                .hostUserId(1L)
                .name("햇살 가득한 마당")
                .build();
    }

    /** ReviewResponse 는 생성자도 세터도 없어 목으로 만든다. */
    private ReviewResponse review() {
        ReviewResponse review = mock(ReviewResponse.class);
        given(review.getId()).willReturn(11L);
        given(review.getPlaceId()).willReturn(3L);
        given(review.getMemberId()).willReturn(7L);
        given(review.getPlaceName()).willReturn("햇살 가득한 마당");
        given(review.getRating()).willReturn(5);
        given(review.getContent()).willReturn("마당이 넓어서 좋았어요");
        given(review.getCheckInAt()).willReturn(LocalDate.of(2026, 7, 18));
        return review;
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
