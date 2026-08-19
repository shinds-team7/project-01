package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.session.LoginRedirect;
import com.example.petnow.config.KakaoProperties;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.service.AuthService;
import com.example.petnow.service.KakaoService;
import com.example.petnow.service.PetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "로그인하느라 끊긴 흐름을 이어 붙인다"는 약속을 지키는지 본다. (이슈 #164)
 *
 * <p>인터셉터가 목적지를 세션에 맡기고 → 로그인 성공이 그걸 꺼내 되돌려 보내는 왕복 전체가
 * 대상이라, 컨트롤러 하나만 띄우지 않고 {@code AuthController} 와 보호된 화면을 같이 올린다.
 * MockMvc 는 요청 사이에 세션을 이어 주지 않으므로 같은 {@link MockHttpSession} 을 손으로 넘긴다.
 */
@WebMvcTest(controllers = {
        AuthController.class,
        MyPageController.class,
        PetController.class
})
@Import(LoginRedirectFlowTest.UnprotectedProbeController.class)
class LoginRedirectFlowTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    KakaoProperties kakaoProperties;

    @MockitoBean
    KakaoService kakaoService;

    @MockitoBean
    PetService petService;

    @Test
    @DisplayName("로그인하느라 끊긴 화면으로 되돌아간다 — 쿼리스트링까지 그대로")
    void returnsToTheInterruptedPage() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // 1. 비로그인으로 보호된 화면에 들어가려다 로그인 화면으로 밀린다.
        //    (param() 이 아니라 URL 에 직접 붙여야 MockMvc 가 queryString 까지 채운다)
        mockMvc.perform(get("/mypage?tab=pets").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        assertThat(session.getAttribute(SessionConst.REDIRECT_URI)).isEqualTo("/mypage?tab=pets");

        // 2. 로그인에 성공하면 홈이 아니라 원래 가려던 곳으로 간다.
        given(authService.login(any(UserLoginRequest.class))).willReturn(loginUser());

        mockMvc.perform(loginRequest().session(session))
                .andExpect(redirectedUrl("/mypage?tab=pets"));

        // 3. 한 번 쓴 목적지는 남지 않는다. 다음 로그인이 엉뚱한 곳으로 가면 안 된다.
        assertThat(session.getAttribute(SessionConst.REDIRECT_URI)).isNull();
    }

    @Test
    @DisplayName("POST 로 막힌 요청은 목적지로 기억하지 않는다 — 되돌아가 봐야 405 다")
    void doesNotRememberNonGetRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/pet/delete/42").session(session))
                .andExpect(redirectedUrl("/auth/login"));

        assertThat(session.getAttribute(SessionConst.REDIRECT_URI)).isNull();

        given(authService.login(any(UserLoginRequest.class))).willReturn(loginUser());

        mockMvc.perform(loginRequest().session(session))
                .andExpect(redirectedUrl(LoginRedirect.DEFAULT_URI));
    }

    @Test
    @DisplayName("그냥 로그인 화면으로 온 사람은 홈으로 보낸다")
    void goesHomeWhenNothingWasInterrupted() throws Exception {
        given(authService.login(any(UserLoginRequest.class))).willReturn(loginUser());

        mockMvc.perform(loginRequest())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("바깥 주소가 목적지로 심겨 있으면 무시하고 홈으로 — 오픈 리다이렉트 방어")
    void refusesExternalRedirectTarget() throws Exception {
        given(authService.login(any(UserLoginRequest.class))).willReturn(loginUser());

        // "//evil.com" 은 브라우저가 프로토콜 상대 URL 로 읽어 외부 호스트로 나간다.
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.REDIRECT_URI, "//evil.com/steal");

        mockMvc.perform(loginRequest().session(session))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("로그인에 성공하면 세션 id 를 새로 뽑는다 — 세션 고정 공격 차단")
    void rotatesSessionIdOnLogin() throws Exception {
        given(authService.login(any(UserLoginRequest.class))).willReturn(loginUser());

        MockHttpSession session = new MockHttpSession();
        String idBefore = session.getId();

        mockMvc.perform(loginRequest().session(session))
                .andExpect(status().is3xxRedirection());

        assertThat(session.getId()).isNotEqualTo(idBefore);
        assertThat(session.getAttribute(SessionConst.LOGIN_USER_ID)).isEqualTo(7L);
    }

    @Test
    @DisplayName("이미 로그인한 사람에게는 로그인·회원가입 폼을 다시 보여 주지 않는다")
    void sendsLoggedInVisitorAwayFromAuthForms() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 7L);

        mockMvc.perform(get("/auth/login").session(session))
                .andExpect(redirectedUrl("/home"));

        mockMvc.perform(get("/auth/signup").session(session))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("인터셉터가 막지 않는 경로에 @LoginUser 를 붙이면 NPE 대신 401 로 분명히 실패한다")
    void loginUserOutsideInterceptorFailsLoudly() throws Exception {
        // WebConfig 의 경로 목록에서 빠뜨린 화면에 @LoginUser 를 다는 실수를 잡는 안전망이다.
        mockMvc.perform(get("/probe/login-user"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest() {
        return post("/auth/login")
                .param("email", "a@b.c")
                .param("password", "password1!");
    }

    private com.example.petnow.dto.response.LoginUser loginUser() {
        return com.example.petnow.dto.response.LoginUser.builder()
                .id(7L)
                .nickname("초코")
                .email("a@b.c")
                .build();
    }

    /** 보호 목록에 없는 경로. 리졸버가 스스로를 지키는지 보려고만 존재한다. */
    @Controller
    static class UnprotectedProbeController {

        @GetMapping("/probe/login-user")
        @ResponseBody
        String probe(@LoginUser Long loginUserId) {
            return String.valueOf(loginUserId);
        }
    }
}
