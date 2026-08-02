package com.example.petnow.common.util;

/**
 * CodeRabbit 연동 확인용 임시 클래스입니다.
 * 리뷰 봇 동작 확인이 끝나면 삭제합니다.
 */
public class CodeRabbitTestUtil {

    public static String maskEmail(String email) {
        int at = email.indexOf("@");
        String local = email.substring(0, at);
        return local.charAt(0) + "***" + email.substring(at);
    }

    public static int divide(int a, int b) {
        return a / b;
    }
}
