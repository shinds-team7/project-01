package com.example.petnow.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 키 두 종류가 서로 자리를 바꿔 들어가지 않았는지 기동 시점에 확인한다. (#277)
 *
 * <p>카카오는 성격이 완전히 다른 키를 발급한다.
 * <ul>
 *   <li><b>JavaScript 키</b> — {@code kakao.map.javascript-key}. 지도 SDK 가 브라우저에서 쓴다.
 *       HTML 에 그대로 실려 나가고, 그게 정상이다. 숨길 방법이 없으므로 카카오 개발자 콘솔의
 *       <i>플랫폼 &gt; 웹</i> 에 도메인({@code http://localhost:8080}, {@code https://petnow.duckdns.org})을
 *       등록해 사용처를 제한하는 것으로 막는다.</li>
 *   <li><b>REST API 키</b> — {@code kakao.local-api.rest-api-key}. 서버가 주소를 좌표로 바꿀 때 쓴다(#265).
 *       유출되면 우리 쿼터로 아무나 API 를 호출할 수 있다. 브라우저로 내려가면 안 된다.</li>
 * </ul>
 *
 * <p>두 키는 형태가 같은 32자 문자열이라 눈으로는 구분되지 않는다. 그래서 REST 키를 JavaScript 키
 * 자리에 붙여 넣는 사고는 조용히 성공하고, 그 뒤로 모든 방문자에게 서버 키가 노출된다. 여기서
 * 기동을 실패시키는 편이 낫다. 값이 같다는 것은 둘 중 하나가 잘못 들어갔다는 뜻이다.
 *
 * <p>REST 키는 이 클래스 안에서 비교에만 쓰고 필드로 남기지 않는다. 뷰가 닿을 수 있는 빈에
 * 서버 키가 실려 있으면 언젠가는 템플릿에서 읽힌다.
 */
@Slf4j
@Component
public class KakaoKeyGuard {

    public KakaoKeyGuard(@Value("${kakao.map.javascript-key:}") String javascriptKey,
                         @Value("${kakao.local-api.rest-api-key:}") String restApiKey) {
        String browserKey = trim(javascriptKey);
        String serverKey = trim(restApiKey);

        if (!browserKey.isEmpty() && browserKey.equals(serverKey)) {
            throw new IllegalStateException(
                    "kakao.map.javascript-key 와 kakao.local-api.rest-api-key 가 같은 값이다. "
                            + "JavaScript 키는 브라우저로 내려가므로, 이대로 두면 서버 전용 REST API 키가 "
                            + "모든 방문자에게 노출된다. 카카오 개발자 콘솔에서 두 키를 각각 확인해 다시 넣을 것.");
        }

        if (browserKey.isEmpty()) {
            // 키가 없어도 앱은 떠야 한다. /nearby 는 지도 없이 목록만 그리고 안내 문구를 띄운다.
            log.warn("kakao.map.javascript-key 가 비어 있어 내 주변 화면의 지도를 표시하지 않는다.");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
