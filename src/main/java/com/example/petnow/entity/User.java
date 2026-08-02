package com.example.petnow.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * users 테이블 매핑. 필드명은 ERD 컬럼명 기준.
 */
@Getter
@NoArgsConstructor
public class User {

    private Long userId;
    private String email;
    private String nickname;
    private String password;

    @Builder
    public User(String email, String nickname, String password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }
}
