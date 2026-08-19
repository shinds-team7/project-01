package com.example.petnow.dto.response;

import com.example.petnow.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileResponse {
    private String nickname;
    private String email;
    private String phone;
    private String profileImageUrl;

    public static MyProfileResponse from(User user) {
        return MyProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
