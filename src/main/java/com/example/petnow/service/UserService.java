package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;

public interface UserService {
    void signup(UserSignupRequest request);

    Long login(UserLoginRequest request);
}
