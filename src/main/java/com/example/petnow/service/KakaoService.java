package com.example.petnow.service;

import com.example.petnow.dto.response.KakaoUserResponse;

public interface KakaoService {

    String getAccessToken(String code);

    KakaoUserResponse getUserInfo(String accessToken);

}
