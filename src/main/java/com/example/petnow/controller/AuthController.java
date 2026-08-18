package com.example.petnow.controller;

import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ────────────────────────── 화면 ──────────────────────────

    /**
     * 파라미터명을 그대로 두면 모델에 {@code userLoginRequest} 로 담겨
     * login.html 의 {@code th:object="${userLoginRequest}"} 가 바인딩된다.
     */
    @GetMapping("/login")
    public String loginForm(@ModelAttribute UserLoginRequest userLoginRequest) {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupForm(@ModelAttribute UserSignupRequest userSignupRequest) {
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
                            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            LoginSession.set(session, authService.login(userLoginRequest));
        } catch (BusinessException e) {
            // 그대로 던지면 MvcExceptionHandler 가 잡아 error 페이지로 간다.
            // 폼 안의 배너에 그리려면 global 에러로 넘겨야 한다.
            bindingResult.reject("loginFail", e.getMessage());
            return "auth/login";
        }

        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        LoginSession.clear(session);
        return "redirect:/home";
    }
}
