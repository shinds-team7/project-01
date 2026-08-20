package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.KakaoUserResponse;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.User;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.mapper.AuthMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    /** 가입되지 않은 이메일로 로그인을 시도했을 때 대신 비교할 비밀번호. 값 자체는 아무 의미 없다. */
    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@link #USER_NOT_FOUND_PASSWORD} 를 미리 해시해 둔 값.
     *
     * <p>요청마다 만들면 그 비용이 또 다른 시간 차가 되므로 기동 때 한 번만 만든다.
     * 상수로 박지 않는 이유는, {@code PasswordEncoder} 의 종류나 강도를 바꿔도
     * 이 값이 자동으로 따라와야 실제 로그인 경로와 비용이 같게 유지되기 때문이다.
     */
    private final String userNotFoundEncodedPassword;

    public AuthServiceImpl(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.userNotFoundEncodedPassword = passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
    }

    /**
     * 회원 가입.
     *
     * <p>이메일 중복을 두 겹으로 막는다. 사전 검사만으로는 부족하다. 두 요청이 나란히
     * "없음"을 확인하고 둘 다 INSERT 하는 창이 남기 때문이다. 그 경합에서 뚫린 요청은
     * UNIQUE 제약(V13)에 걸려 {@link DuplicateKeyException} 으로 오는데, 그대로 두면
     * 사용자에게 500 이 나가므로 여기서 같은 예외로 바꿔 준다.
     *
     * <p>사전 검사를 없애고 제약에만 맡기지 않는 이유는, 흔한 경우(그냥 이미 가입된
     * 이메일)에 굳이 INSERT 를 시도해 실패시킬 필요가 없어서다.
     */
    @Override
    public void signup(UserSignupRequest request) {

        if (authMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        try {
            authMapper.signup(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 로그인.
     *
     * <p>실패 사유를 둘 다 {@link AuthErrorCode#INVALID_CREDENTIALS} 로 뭉뚱그리는 것만으로는
     * 가입 여부를 감출 수 없다. 사용자를 못 찾았을 때 그대로 반환하면 BCrypt 비교를 건너뛰어
     * 응답이 눈에 띄게 빨라지고, 그 시간 차만으로 "이 이메일은 가입돼 있다"를 알 수 있다.
     * BCrypt 는 일부러 느린 해시라 차이가 수십 ms 로 벌어진다.
     *
     * <p>그래서 못 찾았을 때도 더미 해시에 대고 비교를 한 번 돌려 같은 비용을 치른다.
     * 스프링 시큐리티의 {@code DaoAuthenticationProvider} 가 쓰는 방법과 같다.
     */
    @Override
    public LoginUser login(UserLoginRequest request) {

        User user = authMapper.findByEmail(request.getEmail());

        if (user == null) {
            mitigateAgainstTimingAttack(request.getPassword());
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return LoginUser.from(user);
    }

    /**
     * 비교할 상대가 없어도 비교한 만큼의 시간을 쓴다.
     *
     * <p>결과는 항상 false 라 쓰지 않는다. 호출하는 것 자체가 목적이므로 지우면 안 된다.
     */
    private void mitigateAgainstTimingAttack(String rawPassword) {
        passwordEncoder.matches(rawPassword, userNotFoundEncodedPassword);

    }

    @Override
    public UserMyPageResponse getMyPage(Long userId) {

        User user = authMapper.findById(userId);

        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return  UserMyPageResponse.from(user);
    }

    @Override
    public LoginUser loginOrSignupKakao(KakaoUserResponse kakaoUser) {

        User user = authMapper.findByProviderAndProviderId(
            "KAKAO",
            kakaoUser.getKakaoId().toString()
        );

        if (user == null) {

            user = User.builder()
                .nickname(kakaoUser.getNickname())
                .provider("KAKAO")
                .providerId(kakaoUser.getKakaoId().toString())
                .build();

            authMapper.signupKakao(user);
        }

        return LoginUser.from(user);
    }
}
