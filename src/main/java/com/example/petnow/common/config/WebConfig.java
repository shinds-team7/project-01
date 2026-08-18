package com.example.petnow.common.config;

import com.example.petnow.common.argument.LoginUserArgumentResolver;
import com.example.petnow.common.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    /**
     * 로그인이 필요한 URL 목록. <b>이 목록이 곧 접근 제어 정책이다.</b>
     *
     * <p>여기 걸린 경로의 컨트롤러는 세션을 직접 확인하지 않고
     * {@link com.example.petnow.common.argument.LoginUser} 로 사용자 id 를 받는다.
     * 반대로 목록에 없는 경로에 {@code @LoginUser} 를 붙이면 401 로 떨어지니,
     * 새 화면을 만들 때는 둘을 항상 같이 손봐야 한다.
     *
     * <p>적어 두는 예외 두 가지.
     * <ul>
     *   <li>{@code /auth/**} 는 애초에 목록에 없다. 로그인·회원가입은 물론 앞으로 붙을
     *       카카오 콜백까지 비로그인 상태로 들어와야 하므로 계속 열려 있어야 한다.</li>
     *   <li>{@code POST /places}(장소 등록)는 공개 목록인 {@code GET /places} 와 주소가 같아
     *       경로 패턴으로 가를 수 없다. 그래서 {@code PlaceController} 안에서 직접 막는다.
     *       진입점인 {@code /places/new} 는 여기서 막으므로 실사용 흐름은 로그인 화면을 거친다.</li>
     * </ul>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
            .addPathPatterns(
                "/mypage/**",
                "/my/**",
                "/pet/**",
                "/host/**",
                "/reviews/**",
                "/reservation/**",
                "/places/new"
            )
            // 장소별 리뷰 목록은 로그인 없이 볼 수 있어야 한다.
            .excludePathPatterns("/reviews/place/**");
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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
