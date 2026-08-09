package com.example.petnow.common.constant;

public class SessionConst {

    // 유저정보 세션 키 상수로 만듦
    public static final String LOGIN_USER_ID = "loginUserId";

    /**
     * 화면 표시용 로그인 사용자({@link com.example.petnow.dto.response.LoginUser}).
     * 템플릿에서는 {@code ${session.loginUser}} 로 읽는다.
     */
    public static final String LOGIN_USER = "loginUser";

    private SessionConst() {}
}
