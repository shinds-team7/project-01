package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.service.HostService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 호스트 홈(#179)과 장소 삭제(#189)를 확인한다.
 *
 * <p>#189 에서 팀원이 지적한 세 가지가 화면에 실제로 그려지는지 본다.
 * 마크업까지 함께 단정해야 라벨만 남고 링크가 빠지는 회귀를 잡을 수 있다.
 */
@WebMvcTest(HostController.class)
class HostDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HostService hostService;

    // ────────────────────── #189 화면 ──────────────────────

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
    @DisplayName("카드 액션이 프로토타입대로 일정 관리·리뷰 관리·삭제 세 개다")
    void placeCardShowsPrototypeActions() throws Exception {
        given(hostService.getPlacesByUserId(1L)).willReturn(List.of(place()));

        mockMvc.perform(get("/host").session(loggedIn()))
                .andExpect(status().isOk())
                // 서버 기능이 없어 비활성이지만 라벨은 프로토타입 그대로 둔다
                .andExpect(content().string(containsString("일정 관리 및 수정")))
                .andExpect(content().string(containsString("리뷰 관리")))
                .andExpect(content().string(containsString("/reviews/place/3")))
                // 이식 과정에서 사라졌던 삭제 버튼과 확인 모달
                .andExpect(content().string(containsString("action=\"/host/places/3/delete\"")))
                .andExpect(content().string(containsString("게시글을 삭제할까요?")))
                // 삭제 버튼과 모달이 id 로 이어져 있어야 modal.js 가 열 수 있다
                .andExpect(content().string(containsString("data-modal-open")))
                .andExpect(content().string(containsString("aria-controls=\"place-delete-3\"")))
                .andExpect(content().string(containsString("id=\"place-delete-3\"")))
                // 이 자리에 있던 "내 공개 화면 보기" 는 카드 링크로 옮겼다
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
    @DisplayName("예약 요청 탭은 호스트용 API 가 없다는 걸 화면으로 알린다")
    void bookingTabShowsPendingNotice() throws Exception {
        mockMvc.perform(get("/host").param("tab", "booking").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("예약 요청을 불러올 수 없어요")))
                // 장소 탭 내용이 함께 나오면 안 된다
                .andExpect(content().string(not(containsString("새 장소 등록하기"))));
    }

    // ────────────────────── 삭제 ──────────────────────

    @Test
    @DisplayName("삭제하면 세션 주인의 userId 로 위임하고 호스트 홈으로 되돌린다")
    void deleteDelegatesWithSessionUser() throws Exception {
        mockMvc.perform(post("/host/places/3/delete").session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host"));

        then(hostService).should().deletePlace(1L, 3L);
    }

    @Test
    @DisplayName("비로그인 삭제 요청은 서비스를 부르지 않고 홈으로 돌려보낸다")
    void deleteRejectsAnonymous() throws Exception {
        mockMvc.perform(post("/host/places/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        then(hostService).should(never()).deletePlace(anyLong(), anyLong());
    }

    @Test
    @DisplayName("남의 장소 ID 를 보내도 세션 주인의 userId 로만 삭제를 시도한다")
    void deleteAlwaysUsesSessionUser() throws Exception {
        // 소유자 검증은 서비스가 UPDATE 의 WHERE 절로 한다.
        // 컨트롤러는 요청 본문의 userId 를 절대 믿지 않는다는 것만 여기서 못박는다.
        mockMvc.perform(post("/host/places/999/delete").param("userId", "42").session(loggedIn()))
                .andExpect(status().is3xxRedirection());

        then(hostService).should().deletePlace(1L, 999L);
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
