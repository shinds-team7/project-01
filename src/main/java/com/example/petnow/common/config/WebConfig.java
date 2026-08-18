package com.example.petnow.common.config;

import com.example.petnow.common.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 전역 설정.
 *
 * 여기 한 곳만 보면 "어떤 URL 이 로그인 필요한지", "어떤 파라미터가 자동 주입되는지"를
 * 전부 알 수 있게 유지한다. 새 화면을 추가할 때 이 파일을 꼭 확인할 것.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LocalStorageProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final LocalStorageProperties localStorageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
            .addPathPatterns(
                "/mypage/**",
                "/my/**",
                "/pet/**",
                "/host/**",
                "/reviews/**",
                "/reservation/**"
            )
            .excludePathPatterns(
                "/",
                "/home",
                "/places/**",
                "/auth/**"
            );
    }
    /**
     * 로컬에 저장한 업로드 이미지를 열어 주는 매핑.
     *
     * <p>{@code LocalFileStorage} 가 쓰는 디렉터리는 classpath 밖이라 그냥 두면 404 다.
     * 운영에서는 이미지가 S3 URL 로 나가므로 이 경로로 요청이 오지 않는다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(localStorageProperties.getUrlPrefix() + "/**")
                .addResourceLocations(localStorageProperties.resolveResourceLocation());
    }
}
