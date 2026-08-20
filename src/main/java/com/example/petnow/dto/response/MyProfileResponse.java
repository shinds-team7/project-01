package com.example.petnow.dto.response;

import com.example.petnow.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponse {
    private static final String KAKAO_PROVIDER = "KAKAO";

    private String nickname;
    private String email;
    private String phone;
    private String profileImageUrl;
    private String provider;

    public boolean isKakaoUser() {
        return KAKAO_PROVIDER.equals(provider);
    }

    public static MyProfileResponse from(User user) {
        return MyProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .build();
    }
}
