package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.service.MyProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyProfileController.class)
class MyProfileWiringTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyProfileService myProfileService;

    @Test
    @DisplayName("내 정보 화면이 회원 탈퇴 확인 모달을 탈퇴 엔드포인트에 연결한다")
    void profileWiresWithdrawAction() throws Exception {
        given(myProfileService.getProfile(1L)).willReturn(MyProfileResponse.builder()
                .nickname("지우")
                .email("jiwoo@petnow.kr")
                .build());

        mockMvc.perform(get("/my/profile").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-controls=\"withdraw-modal\"")))
                .andExpect(content().string(containsString("action=\"/my/withdraw\"")))
                .andExpect(content().string(containsString("/js/modal.js")))
                .andExpect(content().string(containsString("탈퇴 즉시 로그아웃")));
    }

    @Test
    @DisplayName("회원 탈퇴 요청이 서비스를 호출하고 로그인 세션을 무효화한다")
    void withdrawActionClearsSession() throws Exception {
        MockHttpSession session = loggedIn();

        mockMvc.perform(post("/my/withdraw").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        then(myProfileService).should().withdraw(1L);
        assertTrue(session.isInvalid());
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
