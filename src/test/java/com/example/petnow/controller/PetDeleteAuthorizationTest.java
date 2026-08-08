package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.service.PetService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 반려동물 삭제의 인증·소유자 검증 회귀 테스트 (이슈 #131).
 *
 * <p>이전에는 {@code POST /pet/delete/{petId}} 가 세션을 아예 읽지 않아
 * 비로그인 상태에서도 경로의 ID 만 바꾸면 남의 반려동물을 지울 수 있었습니다.
 */
@WebMvcTest(PetController.class)
class PetDeleteAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @Test
    @DisplayName("비로그인 상태로 삭제를 요청하면 서비스를 호출하지 않고 홈으로 돌려보낸다")
    void deleteWithoutSession_doesNotTouchService() throws Exception {
        mockMvc.perform(post("/pet/delete/42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(petService, never()).deletePet(any(), any());
    }

    @Test
    @DisplayName("로그인 상태면 세션의 userId 와 함께 삭제를 위임하고 마이페이지로 리다이렉트한다")
    void deleteWithSession_delegatesWithLoginUserId() throws Exception {
        HttpSession session = loginAs(1L);

        mockMvc.perform(post("/pet/delete/42").session((MockHttpSession) session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        // petId 만이 아니라 로그인 사용자 ID 도 함께 넘어가야 매퍼가 소유자를 걸러낼 수 있다.
        verify(petService).deletePet(1L, 42L);
    }

    @Test
    @DisplayName("다른 사용자의 반려동물 ID 로 요청해도 세션 주인의 userId 로만 삭제를 시도한다")
    void deleteOthersPet_usesSessionOwnerId() throws Exception {
        HttpSession session = loginAs(1L);

        mockMvc.perform(post("/pet/delete/999").session((MockHttpSession) session))
                .andExpect(status().is3xxRedirection());

        // 경로의 petId 가 남의 것이어도 userId 는 세션에서만 나온다.
        // 실제 차단은 PetMapper.xml 의 user_id 조건이 담당한다.
        verify(petService).deletePet(1L, 999L);
        verify(petService, never()).deletePet(999L, 999L);
    }

    private HttpSession loginAs(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, userId);
        return session;
    }
}
