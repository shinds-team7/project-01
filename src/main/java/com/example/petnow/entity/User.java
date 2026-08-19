package com.example.petnow.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class User {

    private Long id;
    private String email;
    private String nickname;
    private String password;
    private String phone;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String provider;
    private String providerId;

    @Builder
    public User(
        String email,
        String nickname,
        String password,
        String phone,
        String profileImageUrl,
        String provider,
        String providerId
    ) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerId = providerId;
    }
}
