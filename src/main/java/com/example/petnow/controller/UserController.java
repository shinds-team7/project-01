package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.entity.User;
import com.example.petnow.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입. 가입만으로는 세션이 생기지 않으므로 마이페이지가 아니라 홈으로 보냅니다.
    // 홈(HomeController "/")은 장소 목록으로 리다이렉트합니다.
    @PostMapping("/signup")
    public String userSignup(@Valid @ModelAttribute UserSignupRequest request) {
        userService.signup(request);
        return "redirect:/";
    }

    // 로그인
    @PostMapping ("/login")
    public String userLogin(@Valid @ModelAttribute UserLoginRequest request, HttpSession session) {

        Long userId = userService.login(request);

        session.setAttribute(SessionConst.LOGIN_USER_ID, userId);

        // 세션 저장 test
        log.info("세션에 저장된 userId: {}",
                session.getAttribute(SessionConst.LOGIN_USER_ID));

        // 뷰 이름을 그대로 반환하면 모델이 비어 마이페이지가 빈 화면으로 뜨고
        // 새로고침 시 로그인이 재전송됩니다. PRG 패턴으로 /mypage 에 다시 요청합니다.
        return "redirect:/mypage";
    }
}
