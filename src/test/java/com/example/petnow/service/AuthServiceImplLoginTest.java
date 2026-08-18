package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.entity.User;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * 로그인 실패가 "왜 실패했는지"를 흘리지 않는지 본다. (#263)
 *
 * <p>문구는 이미 양쪽 다 같지만(#117), 사용자를 못 찾았을 때 BCrypt 비교를 건너뛰면
 * 응답 시간이 갈려 가입 여부가 그대로 드러난다. 시간 자체는 재도 흔들려서 못 믿으므로,
 * "같은 일을 하는지"를 대신 검증한다.
 */
class AuthServiceImplLoginTest {

    /** 생성자가 더미 비밀번호를 해시해 둘 때 나오는 값. */
    private static final String ENCODED = "encoded";

    private AuthMapper authMapper;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        authMapper = mock(AuthMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        given(passwordEncoder.encode(any())).willReturn(ENCODED);
        service = new AuthServiceImpl(authMapper, passwordEncoder);
    }

    @Test
    @DisplayName("가입되지 않은 이메일이어도 비밀번호 비교를 한 번 돌린다 — 응답 시간으로 가입 여부가 새지 않게")
    void unknownEmailStillPaysThePasswordCheckCost() {
        given(authMapper.findByEmail("nobody@petnow.kr")).willReturn(null);

        assertThatThrownBy(() -> service.login(request("nobody@petnow.kr")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

        // 이 검증이 이 테스트의 전부다. 비교를 건너뛰면 응답이 빨라져 가입 여부가 드러난다.
        then(passwordEncoder).should().matches("pw12345678", ENCODED);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 같은 오류로 막는다 — 없는 이메일과 구분되지 않아야 한다")
    void wrongPasswordFailsWithTheSameError() {
        User user = user();
        given(authMapper.findByEmail("choco@petnow.kr")).willReturn(user);
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> service.login(request("choco@petnow.kr")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호가 맞으면 세션에 담을 로그인 사용자를 돌려준다")
    void correctPasswordReturnsLoginUser() {
        User user = user();
        given(authMapper.findByEmail("choco@petnow.kr")).willReturn(user);
        given(passwordEncoder.matches("pw12345678", "stored-hash")).willReturn(true);

        LoginUser loginUser = service.login(request("choco@petnow.kr"));

        assertThat(loginUser.getId()).isEqualTo(7L);
        assertThat(loginUser.getNickname()).isEqualTo("초코");
    }

    /**
     * id 는 DB 가 채우는 값이라 {@code User.builder()} 로는 넣을 수 없다.
     * 세션에 담기는 값이 id 라 검증에는 필요해서 여기서만 스텁으로 세운다.
     */
    private User user() {
        User user = mock(User.class);
        given(user.getId()).willReturn(7L);
        given(user.getEmail()).willReturn("choco@petnow.kr");
        given(user.getNickname()).willReturn("초코");
        given(user.getPassword()).willReturn("stored-hash");
        return user;
    }

    private UserLoginRequest request(String email) {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail(email);
        request.setPassword("pw12345678");
        return request;
    }
}
