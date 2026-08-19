package com.example.petnow.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 클라이언트 설정. prod 프로필에서만 올라온다.
 *
 * <p>로컬에서는 아예 만들지 않는다. AWS 자격증명이 없는 팀원의 애플리케이션이 뜨다가 죽으면 안 된다.
 * 로컬은 {@code LocalFileStorage} 가 같은 역할을 한다.
 *
 * <p>액세스 키를 코드나 설정 파일에 적지 않고 {@link DefaultCredentialsProvider} 에 맡긴다.
 * EC2 에서는 인스턴스 프로파일(IAM Role)을, 개발 PC 에서는 환경변수나 {@code ~/.aws/credentials} 를
 * 알아서 찾는다. 키가 Git 에 올라갈 일이 구조적으로 없어진다.
 */
@Configuration
@Profile("prod")
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties s3Properties) {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }
}
