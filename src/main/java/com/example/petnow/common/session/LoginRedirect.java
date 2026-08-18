package com.example.petnow.common.session;

import com.example.petnow.common.constant.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 로그인 때문에 끊긴 흐름을 이어 붙이는 통로.
 *
 * <p>비로그인 상태로 보호된 화면에 들어오면 인터셉터가 원래 목적지를 여기에 맡기고,
 * 로그인에 성공한 컨트롤러가 {@link #pop} 으로 되찾아 그리로 보낸다.
 *
 * <p><b>왜 쿼리 파라미터가 아니라 세션인가.</b> 곧 붙일 카카오 로그인은
 * {@code /auth/login → 카카오 → /auth/kakao/callback} 으로 왕복하는데, 카카오는 우리가 붙인
 * 쿼리 파라미터를 콜백에 돌려주지 않는다. 목적지를 세션에 두면 폼 로그인과 소셜 로그인이
 * 같은 코드로 복귀 지점을 찾는다. 덤으로 사용자 입력이 로그인 URL 에 그대로 실려
 * 되비치는 일도 없다.
 */
public final class LoginRedirect {

    /** 되돌아갈 곳을 못 찾았을 때의 기본 목적지. */
    public static final String DEFAULT_URI = "/home";

    private LoginRedirect() {}

    /**
     * 지금 요청을 "로그인 뒤 돌아올 곳"으로 세션에 적어 둔다.
     *
     * <p>GET 만 기억한다. {@code POST /pet/create} 를 적어 두면 로그인 후 그 주소로 GET 을
     * 보내게 되는데, 그런 매핑이 없어 405 가 나고 사용자가 채운 폼도 어차피 이미 사라진 뒤다.
     * 이때는 아무것도 적지 않아 {@link #pop} 이 기본 목적지를 돌려주게 둔다.
     */
    public static void save(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String query = request.getQueryString();
        String uri = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;

        // 여기까지 왔으면 세션이 없을 수 있다. 목적지를 적으려면 만들어야 한다.
        request.getSession().setAttribute(SessionConst.REDIRECT_URI, uri);
    }

    /**
     * 적어 둔 목적지를 꺼내면서 지운다. 한 번 쓰고 버려야 다음 로그인까지 남지 않는다.
     *
     * @return 안전한 내부 경로. 적어 둔 게 없거나 수상하면 {@link #DEFAULT_URI}
     */
    public static String pop(HttpSession session) {
        Object saved = session.getAttribute(SessionConst.REDIRECT_URI);
        session.removeAttribute(SessionConst.REDIRECT_URI);

        return saved instanceof String uri && isInternalPath(uri) ? uri : DEFAULT_URI;
    }

    /**
     * 우리 서비스 안의 경로인지 본다.
     *
     * <p>지금은 인터셉터가 넣어 주는 값만 들어오지만, 나중에 {@code ?redirectUri=} 같은
     * 파라미터를 받게 되면 이 검사가 없는 순간 오픈 리다이렉트가 된다.
     * {@code //evil.com} 과 {@code /\evil.com} 은 브라우저가 외부 호스트로 해석한다.
     */
    private static boolean isInternalPath(String uri) {
        return uri.startsWith("/")
                && !uri.startsWith("//")
                && !uri.startsWith("/\\");
    }
}
