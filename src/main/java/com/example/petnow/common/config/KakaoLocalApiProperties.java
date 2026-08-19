package com.example.petnow.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "kakao.local-api")
public class KakaoLocalApiProperties {

    private String baseUrl = "https://dapi.kakao.com";
    private String restApiKey = "";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(3);
    private boolean backfillEnabled;
    private int backfillBatchSize = 100;
}
