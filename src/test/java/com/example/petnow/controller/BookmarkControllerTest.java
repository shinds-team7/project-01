package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.BookmarkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    @Test
    @DisplayName("찜 페이지는 로그인 사용자의 찜 목록을 그린다")
    void rendersBookmarkedPlaces() throws Exception {
        given(bookmarkService.getBookmarkedPlaces(7L)).willReturn(List.of(place()));

        mockMvc.perform(get("/bookmarks").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("bookmarks"))
                .andExpect(model().attributeExists("places"))
                .andExpect(content().string(containsString("마당 있는 집")))
                .andExpect(content().string(containsString("/places/3")));
    }

    @Test
    @DisplayName("찜 목록 API는 로그인 사용자의 찜 목록을 JSON으로 돌려준다")
    void returnsBookmarkedPlacesAsJson() throws Exception {
        given(bookmarkService.getBookmarkedPlaces(7L)).willReturn(List.of(place()));

        mockMvc.perform(get("/api/bookmarks").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].bookmarked").value(true));
    }

    @Test
    @DisplayName("북마크 버튼은 현재 사용자와 장소 기준으로 비동기 토글 결과를 돌려준다")
    void togglesBookmark() throws Exception {
        given(bookmarkService.toggle(7L, 3L)).willReturn(true);

        mockMvc.perform(post("/api/bookmarks/3/toggle").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true));

        then(bookmarkService).should().toggle(7L, 3L);
    }

    private PlaceListResponse place() {
        return PlaceListResponse.builder()
                .id(3L)
                .name("마당 있는 집")
                .nickname("호스트")
                .placeType(PlaceType.HOUSE)
                .hourlyPrice(BigDecimal.valueOf(12000))
                .bookmarked(true)
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 7L);
        return session;
    }
}
