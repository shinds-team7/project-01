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
    //private String profileImageUrl; 추후 재선언 예정

    public static MyProfileResponse from(User user) {
        return MyProfileResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
