package com.example.petnow.dto.response;

import com.example.petnow.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * 세션에 담아 두는 로그인 사용자 요약 정보.
 *
 * <p>화면 헤더에 "○○ 보호자님" 을 그리는 데만 쓴다. 매 요청마다 사용자 정보를 다시 조회하지
 * 않으려고 로그인 시점에 한 번 담아 두는 값이므로, <b>닉네임이 바뀌는 곳에서는 반드시
 * {@link com.example.petnow.common.session.LoginSession#set} 으로 세션을 다시 채워야 한다.</b>
 * 그렇지 않으면 헤더에 예전 닉네임이 남는다.
 *
 * <p>세션에 들어가므로 비밀번호 같은 민감한 값은 담지 않는다.
 */
@Getter
@Builder
public class LoginUser implements Serializable {

    private final Long id;
    private final String nickname;
    private final String email;

    public static LoginUser from(User user) {
        return LoginUser.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }

    /** 헤더 아바타에 쓰는 닉네임 첫 글자. */
    public String getInitial() {
        return (nickname == null || nickname.isBlank()) ? "?" : nickname.substring(0, 1);
    }
}
