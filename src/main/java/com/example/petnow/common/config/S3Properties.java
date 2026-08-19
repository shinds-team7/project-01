package com.example.petnow.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code cloud.aws.s3.*} 설정. 값은 전부 환경변수로 주입한다.
 *
 * <p>이 클래스는 prod 프로필에서만 등록된다. ({@link S3Config})
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class S3Properties {

    /**
     * 이미지를 올릴 버킷 이름.
     */
    private String bucket;

    /**
     * 버킷이 있는 리전. 예: {@code ap-northeast-2}
     */
    private String region;

    /**
     * 저장된 이미지를 읽을 때 쓰는 URL 앞부분. 나중에 CloudFront 로 옮기면 이 값만 바꾼다.
     *
     * <p>비워 두면 버킷·리전으로 기본 S3 URL 을 만들어 쓴다.
     */
    private String baseUrl;
}
