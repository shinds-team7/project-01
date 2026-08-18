package com.example.petnow.common.interceptor;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.session.LoginRedirect;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인이 필요한 화면을 한곳에서 막는다.
 *
 * <p>어떤 URL 이 여기에 걸리는지는 {@link com.example.petnow.common.config.WebConfig} 에만 적혀 있다.
 * 컨트롤러는 "이미 로그인한 사용자만 들어온다"를 전제로 쓰면 되고,
 * 로그인 사용자 id 는 {@link com.example.petnow.common.argument.LoginUser} 로 받는다.
 */
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (isLoggedIn(request)) {
            return true;
        }

        // 로그인 후 원래 가려던 곳으로 돌려보내기 위해 목적지를 맡겨 둔다.
        LoginRedirect.save(request);

        response.sendRedirect("/auth/login");
        return false;
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        return session != null && session.getAttribute(SessionConst.LOGIN_USER_ID) != null;
    }
}
