package com.example.petnow.dto.response;

import com.example.petnow.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMyPageResponse {

    private String nickname;
    private String email;

    public static UserMyPageResponse from(User user) {
        return UserMyPageResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }
}
