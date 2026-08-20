package com.example.petnow.service;

import com.example.petnow.config.KakaoProperties;
import com.example.petnow.dto.response.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {

    private final KakaoProperties kakaoProperties;

    private final RestClient restClient = RestClient.create();

    @Override
    public String getAccessToken(String code) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoProperties.getClientId());
        formData.add("client_secret", kakaoProperties.getClientSecret());
        formData.add("redirect_uri", kakaoProperties.getRedirectUri());
        formData.add("code", code);

        Map response = restClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(Map.class);

        return (String) response.get("access_token");
    }

    @Override
    public KakaoUserResponse getUserInfo(String accessToken) {

        Map response = restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .body(Map.class);

        Long kakaoId = ((Number) response.get("id")).longValue();

        Map<String, Object> kakaoAccount =
            (Map<String, Object>) response.get("kakao_account");

        Map<String, Object> profile =
            (Map<String, Object>) kakaoAccount.get("profile");

        String nickname =
            (String) profile.get("nickname");

        return KakaoUserResponse.builder()
            .kakaoId(kakaoId)
            .nickname(nickname)
            .build();
    }
}
