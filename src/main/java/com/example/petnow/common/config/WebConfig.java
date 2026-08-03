package com.example.petnow.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 전역 설정.
 *
 * 여기 한 곳만 보면 "어떤 URL 이 로그인 필요한지", "어떤 파라미터가 자동 주입되는지"를
 * 전부 알 수 있게 유지한다. 새 화면을 추가할 때 이 파일을 꼭 확인할 것.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {


}
