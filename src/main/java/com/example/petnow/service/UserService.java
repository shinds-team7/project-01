package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.User;

public interface UserService {
    void signup(UserSignupRequest request);

    Long login(UserLoginRequest request);

    UserMyPageResponse getMyPage(Long userId);
}
