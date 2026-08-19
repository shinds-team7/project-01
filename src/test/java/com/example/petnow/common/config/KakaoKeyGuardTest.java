package com.example.petnow.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 카카오 키 두 종류가 뒤바뀐 채 배포되지 않게 막는다. (#277)
 *
 * <p>JavaScript 키는 화면에 그대로 실려 나간다. 그 자리에 서버 전용 REST API 키가 들어가면
 * 예외 하나 없이 잘 뜨고, 그때부터 모든 방문자가 서버 키를 받아 간다. 눈으로는 구분되지 않는
 * 32자 문자열이라 코드 리뷰로도 걸러지지 않는다. 기동을 실패시키는 쪽이 낫다.
 */
class KakaoKeyGuardTest {

    private static final String SAME_KEY = "0123456789abcdef0123456789abcdef";

    @Test
    @DisplayName("JavaScript 키 자리에 REST API 키가 들어가면 기동을 막는다")
    void rejectsSwappedKeys() {
        assertThatThrownBy(() -> new KakaoKeyGuard(SAME_KEY, SAME_KEY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("kakao.map.javascript-key");
    }

    @Test
    @DisplayName("앞뒤 공백만 다른 같은 키도 같은 값으로 본다")
    void rejectsSwappedKeysIgnoringWhitespace() {
        assertThatThrownBy(() -> new KakaoKeyGuard("  " + SAME_KEY + "  ", SAME_KEY))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("서로 다른 두 키는 정상으로 본다")
    void acceptsDistinctKeys() {
        assertThatCode(() -> new KakaoKeyGuard(SAME_KEY, "fedcba9876543210fedcba9876543210"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("키가 하나도 없어도 앱은 뜬다")
    void allowsMissingKeys() {
        // 키 없이 클론한 팀원의 로컬에서도 앱은 떠야 한다. /nearby 가 지도 없이 목록만 그린다.
        assertThatCode(() -> new KakaoKeyGuard("", "")).doesNotThrowAnyException();
        assertThatCode(() -> new KakaoKeyGuard(null, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("REST 키만 있고 JavaScript 키가 비어 있으면 비교하지 않는다")
    void ignoresBlankJavascriptKey() {
        // 둘 다 빈 값이라 '같다'로 판정되면 지도 키를 안 넣은 환경이 전부 죽는다.
        assertThatCode(() -> new KakaoKeyGuard("", SAME_KEY)).doesNotThrowAnyException();
    }
}
