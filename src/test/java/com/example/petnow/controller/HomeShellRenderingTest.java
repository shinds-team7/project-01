package com.example.petnow.controller;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 홈과 하단 네비 목적지가 프로토타입 앱 셸로 그려지는지 확인한다.
 *
 * <p>셸이 빠지면 화면이 스타일 없는 맨 HTML 로 나오므로 app.css 링크 유무로 잡는다.
 */
@WebMvcTest(HomeController.class)
class HomeShellRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @Test
    @DisplayName("홈이 앱 셸과 하단 네비를 갖고 그려진다")
    void homeRendersWithAppShell() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("class=\"app-nav\"")))
                .andExpect(content().string(containsString("우리 동네 <em>펫시터</em>")));
    }

    @Test
    @DisplayName("조회할 장소가 없으면 홈이 500 이 아니라 빈 상태 안내를 그린다")
    void homeRendersEmptyRecentPlaces() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아직 둘러볼 수 있는 호스트가 없어요")));
    }

    @Test
    @DisplayName("하단 네비가 가리키는 검색·내 주변·찜이 404 가 아니라 준비 중 화면을 연다")
    void navDestinationsResolve() throws Exception {
        for (String path : new String[]{"/search", "/nearby", "/bookmarks"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(view().name("coming-soon"))
                    .andExpect(content().string(containsString("화면을 준비하고 있어요")));
        }
    }
}
