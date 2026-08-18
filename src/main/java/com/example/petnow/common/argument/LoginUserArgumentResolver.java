package com.example.petnow.common.argument;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@code @LoginUser Long userId} 파라미터에 세션의 로그인 사용자 id 를 넣어 준다.
 *
 * <p>정상 흐름에서는 {@link com.example.petnow.common.interceptor.LoginCheckInterceptor} 가
 * 먼저 걸러 주므로 여기 도달했다면 이미 로그인 상태다. 그래도 세션을 다시 확인하는 이유는,
 * {@code WebConfig} 의 경로 목록에서 빠진 URL 에 {@code @LoginUser} 를 붙이는 실수가 나면
 * null 을 그대로 흘려보내 서비스 안쪽에서 엉뚱한 NPE 로 터지기 때문이다.
 * 그 경우 "인증이 필요하다"고 제자리에서 분명히 말하고 끝낸다.
 */
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        boolean hasLoginUserAnnotation =
            parameter.hasParameterAnnotation(LoginUser.class);

        boolean hasLongType =
            Long.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginUserAnnotation && hasLongType;
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {

        HttpServletRequest request =
            webRequest.getNativeRequest(HttpServletRequest.class);

        HttpSession session = request == null ? null : request.getSession(false);

        Object loginUserId = session == null ? null : session.getAttribute(SessionConst.LOGIN_USER_ID);

        if (loginUserId == null) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }

        return loginUserId;
    }
}
