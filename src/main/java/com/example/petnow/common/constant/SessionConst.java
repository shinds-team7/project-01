package com.example.petnow.common.constant;

public class SessionConst {

    // 유저정보 세션 키 상수로 만듦
    public static final String LOGIN_USER_ID = "loginUserId";

    /**
     * 화면 표시용 로그인 사용자({@link com.example.petnow.dto.response.LoginUser}).
     * 템플릿에서는 {@code ${session.loginUser}} 로 읽는다.
     */
    public static final String LOGIN_USER = "loginUser";

    /**
     * 로그인 때문에 중단된 원래 목적지.
     *
     * <p>쿼리 파라미터가 아니라 세션에 두는 이유는
     * {@link com.example.petnow.common.session.LoginRedirect} 의 주석에 적어 뒀다.
     */
    public static final String REDIRECT_URI = "redirectUri";

    private SessionConst() {}
}
