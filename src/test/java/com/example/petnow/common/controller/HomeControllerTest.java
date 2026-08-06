package com.example.petnow.common.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petnow.support.RenderDump;

/**
 * 홈 화면이 공용 레이아웃 프래그먼트와 함께 실제로 렌더링되는지 확인한다.
 * 프래그먼트 경로나 파라미터가 틀리면 Thymeleaf 가 예외를 던지므로 조립 오류를 여기서 잡는다.
 */
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("홈은 앱 셸과 함께 렌더링된다")
    void home() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                // 히어로 문구와 검색 카드
                .andExpect(content().string(containsString("우리 동네")))
                .andExpect(content().string(containsString("검색하기")))
                // 공용 하단 네비가 홈 활성 상태로 붙는다
                .andExpect(content().string(containsString("class=\"app-nav\"")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andDo(RenderDump.to("home"));
    }

    @Test
    @DisplayName("아직 준비 중인 하단 네비 목적지는 임시 화면을 보여준다")
    void comingSoon() throws Exception {
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(view().name("coming-soon"))
                .andExpect(content().string(containsString("찜한 호스트")));
    }
}
