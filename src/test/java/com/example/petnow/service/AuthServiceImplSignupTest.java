package com.example.petnow.service;

import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * 이메일 중복 가입 차단 (#132).
 *
 * <p>중복을 허용하면 {@code AuthMapper.findByEmail} 이 {@code selectOne} 이라
 * {@code TooManyResultsException} 이 터져, 중복 가입한 사람뿐 아니라 그 이메일을 먼저
 * 쓰던 원래 사용자까지 로그인이 막힌다. 그래서 사전 검사와 UNIQUE 제약 두 겹으로 막는다.
 */
class AuthServiceImplSignupTest {

    private AuthMapper authMapper;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        authMapper = mock(AuthMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        given(passwordEncoder.encode(any())).willReturn("encoded");
        service = new AuthServiceImpl(authMapper, passwordEncoder);
    }

    @Test
    @DisplayName("이미 쓰이는 이메일이면 INSERT 를 시도하지 않고 막는다")
    void rejectsDuplicateEmailBeforeInsert() {
        given(authMapper.existsByEmail("choco@petnow.kr")).willReturn(true);

        assertThatThrownBy(() -> service.signup(request()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.DUPLICATE_EMAIL);

        then(authMapper).should(never()).signup(any());
    }

    @Test
    @DisplayName("사전 검사를 통과한 동시 요청이 UNIQUE 제약에 걸려도 500 이 아니라 같은 안내로 나간다")
    void mapsUniqueConstraintViolationToSameError() {
        // 두 요청이 나란히 "없음"을 확인하고 둘 다 INSERT 하는 창이 남는다.
        // 사전 검사만으로는 막을 수 없어 제약 위반을 여기서 받아 준다.
        given(authMapper.existsByEmail("choco@petnow.kr")).willReturn(false);
        willThrow(new DuplicateKeyException("uk_users_email"))
                .given(authMapper).signup(any());

        assertThatThrownBy(() -> service.signup(request()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("새 이메일이면 비밀번호를 해시해서 저장한다")
    void savesNewUserWithEncodedPassword() {
        given(authMapper.existsByEmail("choco@petnow.kr")).willReturn(false);

        service.signup(request());

        then(authMapper).should().signup(org.mockito.ArgumentMatchers.argThat(user -> {
            assertThat(user.getEmail()).isEqualTo("choco@petnow.kr");
            // 평문이 그대로 들어가면 안 된다
            assertThat(user.getPassword()).isEqualTo("encoded");
            return true;
        }));
    }

    private UserSignupRequest request() {
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("choco@petnow.kr");
        request.setNickname("초코");
        request.setPassword("pw12345678");
        return request;
    }
}
