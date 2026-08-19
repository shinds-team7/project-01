package com.example.petnow.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoLocalApiProperties.class)
public class KakaoLocalApiConfig {

    @Bean
    @Qualifier("kakaoLocalApiRestClient")
    public RestClient kakaoLocalApiRestClient(KakaoLocalApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory);
        if (StringUtils.hasText(properties.getRestApiKey())) {
            builder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "KakaoAK " + properties.getRestApiKey().trim());
        }
        return builder.build();
    }
}
