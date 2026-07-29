package com.example.petnow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    private String email;
    private String nickname;
    private String password;
}
