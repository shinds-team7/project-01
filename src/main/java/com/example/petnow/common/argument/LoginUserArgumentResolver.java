package com.example.petnow.common.argument;

import com.example.petnow.common.constant.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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

        HttpSession session = request.getSession(false);

        return session.getAttribute(SessionConst.LOGIN_USER_ID);
    }
}
