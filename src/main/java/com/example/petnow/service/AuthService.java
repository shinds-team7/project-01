package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.UserMyPageResponse;

public interface AuthService {
    void signup(UserSignupRequest request);

    /** 로그인 성공 시 세션에 담을 사용자 요약을 돌려준다. 실패하면 BusinessException. */
    LoginUser login(UserLoginRequest request);

    UserMyPageResponse getMyPage(Long userId);
}
