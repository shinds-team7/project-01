package com.example.petnow.dto.response;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Getter
@Builder
public class KakaoUserResponse {

    private Long kakaoId;
    private String nickname;
}
