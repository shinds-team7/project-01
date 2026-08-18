package com.example.petnow.common.interceptor;

import com.example.petnow.common.constant.SessionConst;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(SessionConst.LOGIN_USER_ID) == null) {

            String requestURI = request.getRequestURI();

            String redirectURL = URLEncoder.encode(requestURI, StandardCharsets.UTF_8);

            response.sendRedirect("/auth/login?redirectURL=" + redirectURL);

            return false;
        }

        return true;
    }
}
