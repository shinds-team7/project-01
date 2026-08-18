package com.example.petnow.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code app.storage.local.*} 설정. 로컬 프로필에서 이미지를 어디에 두고 어떤 URL 로 열어 줄지 정한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage.local")
public class LocalStorageProperties {

    /**
     * 이미지를 저장할 디렉터리. 상대 경로면 애플리케이션 실행 위치 기준이다.
     */
    private String directory = "uploads";

    /**
     * 저장한 이미지를 열어 줄 URL 앞부분. {@code WebConfig} 가 이 경로를 정적 리소스로 매핑한다.
     */
    private String urlPrefix = "/uploads";

    /**
     * 설정에 적힌 디렉터리를 절대 경로로 바꿔 준다. 저장할 때와 정적 매핑할 때가 같은 곳을 봐야 한다.
     */
    public Path resolveDirectory() {
        return Paths.get(directory).toAbsolutePath().normalize();
    }

    /**
     * 정적 리소스 매핑에 넣을 {@code file:} 위치.
     *
     * <p>슬래시로 끝나지 않으면 Spring 이 디렉터리가 아니라 파일 이름으로 보고 아무것도 못 찾는다.
     * {@code Path.toUri()} 는 실제로 존재하는 디렉터리에만 슬래시를 붙여 주므로 직접 맞춰 준다.
     */
    public String resolveResourceLocation() {
        String location = resolveDirectory().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
