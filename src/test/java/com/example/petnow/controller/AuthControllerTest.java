package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.service.AuthService;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 로그인 화면 전반(#117) 회귀 테스트.
 *
 * <p>고친 결함이 세 가지라 각각을 따로 잡아 둔다.
 * <ol>
 *   <li>GET 매핑이 없어 로그인·회원가입 화면 자체가 열리지 않던 것</li>
 *   <li>BindingResult 가 없어 검증 실패와 로그인 실패가 error 페이지로 튕기던 것</li>
 *   <li>세션 값이 뷰까지 닿지 않아 헤더가 계속 비로그인이고 이름이 하드코딩이던 것</li>
 * </ol>
 */
@WebMvcTest(controllers = {AuthController.class, HomeController.class})
class AuthControllerTest {

    private static final LoginUser LOGIN_USER =
            LoginUser.builder().id(7L).nickname("초코").email("choco@petnow.kr").build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    /** 홈이 최근 조회한 호스트 자리에 공개 장소를 그리므로 슬라이스에 필요하다. */
    @MockitoBean
    private PlaceService placeService;

    /** 홈이 검색 조건 카드의 반려동물 선택지를 채울 때 쓴다. (#7) */
    @MockitoBean
    private PetService petService;

    // ───────────────────── 1. GET 매핑 (구 #112) ─────────────────────

    @Test
    @DisplayName("GET /auth/login 이 로그인 폼을 연다")
    void loginFormOpens() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                // login.html 의 th:object="${userLoginRequest}" 가 바인딩할 대상
                .andExpect(model().attributeExists("userLoginRequest"));
    }

    @Test
    @DisplayName("GET /auth/signup 이 회원가입 폼을 연다")
    void signupFormOpens() throws Exception {
        mockMvc.perform(get("/auth/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attributeExists("userSignupRequest"));
    }

    // ───────────────────── 2. 에러가 error 페이지로 가지 않는다 ─────────────────────

    @Test
    @DisplayName("이메일 형식이 틀리면 error 페이지가 아니라 로그인 폼이 다시 그려진다")
    void invalidInputRerendersLoginForm() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .param("email", "not-an-email")
                        .param("password", "pw12345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("userLoginRequest", "email"));

        then(authService).should(never()).login(any());
    }

    @Test
    @DisplayName("자격 증명이 틀리면 error 페이지가 아니라 폼 안의 배너로 그려진다")
    void loginFailureRendersInlineBanner() throws Exception {
        given(authService.login(any(UserLoginRequest.class)))
                .willThrow(new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/auth/login")
                        .param("email", "choco@petnow.kr")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasErrors("userLoginRequest"))
                // login.html 의 #fields.errors('global') 배너에 실제로 그려져야 한다
                // (#183 에서 앱 셸로 옮기며 auth.css 의 .auth-banner 가 app.css 의 .notice 로 바뀌었다)
                .andExpect(content().string(allOf(
                        containsString("notice notice-danger"),
                        containsString(AuthErrorCode.INVALID_CREDENTIALS.getDefaultMessage()))));
    }

    @Test
    @DisplayName("닉네임이 비면 회원가입 폼이 다시 그려진다")
    void invalidInputRerendersSignupForm() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("email", "choco@petnow.kr")
                        .param("nickname", "")
                        .param("password", "pw12345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                .andExpect(model().attributeHasFieldErrors("userSignupRequest", "nickname"));

        then(authService).should(never()).signup(any());
    }

    @Test
    @DisplayName("회원가입에 성공하면 로그인 화면으로 보내고 성공 배너 플래그를 넘긴다")
    void signupRedirectsWithBanner() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .param("email", "choco@petnow.kr")
                        .param("nickname", "초코")
                        .param("password", "pw12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("registered", true));
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 error 페이지가 아니라 이메일 필드 에러로 그려진다")
    void duplicateEmailRendersFieldError() throws Exception {
        willThrow(new BusinessException(AuthErrorCode.DUPLICATE_EMAIL))
                .given(authService).signup(any(UserSignupRequest.class));

        mockMvc.perform(post("/auth/signup")
                        .param("email", "choco@petnow.kr")
                        .param("nickname", "초코")
                        .param("password", "pw12345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signup"))
                // 원인이 이메일 한 곳이라 global 이 아니라 필드 에러로 붙는다
                .andExpect(model().attributeHasFieldErrors("userSignupRequest", "email"))
                // signup.html 의 th:errors="*{email}" 에 실제로 그려져야 한다
                .andExpect(content().string(containsString(
                        AuthErrorCode.DUPLICATE_EMAIL.getDefaultMessage())));
    }

    // ───────────────────── 3. 세션 ─────────────────────

    @Test
    @DisplayName("로그인에 성공하면 세션에 id 와 표시용 사용자 정보가 함께 담긴다")
    void loginStoresBothSessionValues() throws Exception {
        given(authService.login(any(UserLoginRequest.class))).willReturn(LOGIN_USER);

        mockMvc.perform(post("/auth/login")
                        .param("email", "choco@petnow.kr")
                        .param("password", "pw12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(request().sessionAttribute(SessionConst.LOGIN_USER_ID, 7L))
                // 표시용 값이 함께 들어가야 헤더에 이름이 그려진다
                .andExpect(request().sessionAttribute(SessionConst.LOGIN_USER, LOGIN_USER));
    }

    @Test
    @DisplayName("로그아웃하면 세션이 버려지고 홈으로 돌아간다")
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("로그아웃은 GET 으로는 되지 않는다 — 이미지 태그나 프리페치로 세션이 끊기면 안 된다")
    void logoutRejectsGet() throws Exception {
        MockHttpSession session = loggedInSession();

        mockMvc.perform(get("/auth/logout").session(session))
                .andExpect(status().isMethodNotAllowed());

        assertThat(session.isInvalid()).isFalse();
    }

    @Test
    @DisplayName("로그인 상태로 홈에 가면 하드코딩이 아니라 실제 닉네임이 그려진다")
    void homeRendersActualNickname() throws Exception {
        mockMvc.perform(get("/home").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("profile-button"),
                        containsString("초코 보호자님"),
                        containsString("/auth/logout"),
                        // 하드코딩된 예시 이름이 남아 있으면 안 된다
                        not(containsString("지우 보호자님")))));
    }

    @Test
    @DisplayName("비로그인 상태의 홈에는 로그인·회원가입 링크만 보인다")
    void homeRendersAuthLinksWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("/auth/signup"),
                        not(containsString("profile-button")))));
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, LOGIN_USER.getId());
        session.setAttribute(SessionConst.LOGIN_USER, LOGIN_USER);
        return session;
    }
}
