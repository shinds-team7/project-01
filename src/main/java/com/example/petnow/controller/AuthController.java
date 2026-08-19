package com.example.petnow.controller;

import com.example.petnow.common.config.KakaoProperties;
import com.example.petnow.common.session.LoginRedirect;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 폼 로그인 · 회원가입 · 로그아웃.
 *
 * <p>{@code /auth/**} 는 로그인 인터셉터가 걸리지 않는 유일한 보호 대상 밖 영역이다.
 * 앞으로 붙일 카카오 로그인도 이 아래({@code /auth/kakao/**})에 들어와야 하고,
 * 인증에 성공한 뒤 세션을 채우고 원래 목적지로 보내는 마무리는
 * {@link #userLogin} 과 똑같이 {@link LoginSession#set} → {@link LoginRedirect#pop} 순서를 따르면 된다.
 */
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final KakaoProperties kakaoProperties;

    // ────────────────────────── 화면 ──────────────────────────

    /**
     * 파라미터명을 그대로 두면 모델에 {@code userLoginRequest} 로 담겨
     * login.html 의 {@code th:object="${userLoginRequest}"} 가 바인딩된다.
     */
    @GetMapping("/login")
    public String loginForm(@ModelAttribute UserLoginRequest userLoginRequest,
                            HttpServletRequest request) {
        // 이미 로그인한 사람에게 폼을 다시 보여 주면 뒤로 가기로 폼에 갇히는 흐름이 생긴다.
        if (LoginSession.isLoggedIn(request)) {
            return "redirect:" + LoginRedirect.DEFAULT_URI;
        }

        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupForm(@ModelAttribute UserSignupRequest userSignupRequest,
                             HttpServletRequest request) {
        if (LoginSession.isLoggedIn(request)) {
            return "redirect:" + LoginRedirect.DEFAULT_URI;
        }

        return "auth/signup";
    }

    // ────────────────────────── 처리 ──────────────────────────

    @PostMapping("/signup")
    public String userSignup(@Valid @ModelAttribute UserSignupRequest userSignupRequest,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        // BindingResult 가 없으면 검증 실패가 예외로 튀어 error 페이지로 간다.
        // 입력값과 필드 에러를 유지한 채 폼을 다시 그린다.
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            authService.signup(userSignupRequest);
        } catch (BusinessException e) {
            // 그대로 던지면 MvcExceptionHandler 가 잡아 error 페이지로 가고 입력이 전부 날아간다.
            // 원인이 이메일 한 곳이라 global 이 아니라 email 필드 에러로 붙인다 (로그인 쪽과 다른 점).
            bindingResult.rejectValue("email", "duplicateEmail", e.getMessage());
            return "auth/signup";
        }

        redirectAttributes.addFlashAttribute("registered", true);   // login.html 의 성공 배너
        return "redirect:/auth/login";
    }

    @PostMapping("/login")
    public String userLogin(@Valid @ModelAttribute UserLoginRequest userLoginRequest,
                            BindingResult bindingResult,
                            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        LoginUser loginUser;
        try {
            loginUser = authService.login(userLoginRequest);
        } catch (BusinessException e) {
            // 그대로 던지면 MvcExceptionHandler 가 잡아 error 페이지로 간다.
            // 폼 안의 배너에 그리려면 global 에러로 넘겨야 한다.
            bindingResult.reject("loginFail", e.getMessage());
            return "auth/login";
        }

        // 인증에 성공한 뒤에야 세션을 만진다. 실패한 시도로 세션이 새로 생기지 않는다.
        HttpSession session = request.getSession();

        // 세션 고정(session fixation) 차단. 공격자가 심어 둔 세션 id 를 그대로 들고 로그인하면
        // 그 id 를 아는 쪽이 로그인된 세션을 함께 쓰게 된다. 담긴 값은 두고 id 만 새로 뽑는다.
        request.changeSessionId();

        LoginSession.set(session, loginUser);

        // 로그인 때문에 끊겼던 화면이 있으면 그리로, 없으면 홈으로.
        return "redirect:" + LoginRedirect.pop(session);
    }

    // 카카오 로그인
    @GetMapping("/kakao")
    public String kakakoLogin() {
        String kakaoAuthUrl =
            "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoProperties.getClientId()
                + "&redirect_uri=" + kakaoProperties.getRedirectUri()
                + "&response_type=code";

        return "redirect:" + kakaoAuthUrl;
    }

    // 카카오 로그인 결과
    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam String code) {

        System.out.println("카카오 인가 코드 = " + code);

        return "redirect:/home";
    }

    /**
     * 로그아웃은 POST 로만 받는다.
     *
     * <p>GET 이면 {@code <img src="/auth/logout">} 한 줄이나 브라우저 프리페치만으로도
     * 남의 세션을 끊을 수 있다. 상태를 바꾸는 요청이라 링크가 아니라 폼으로 보낸다.
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // 이미 세션이 없으면(만료·중복 로그아웃) 버릴 것도 없다. 새로 만들어 지울 이유는 더 없다.
        if (session != null) {
            LoginSession.clear(session);
        }

        return "redirect:/home";
    }
}
