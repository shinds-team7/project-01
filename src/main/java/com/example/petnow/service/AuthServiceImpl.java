package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.User;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입.
     *
     * <p>이메일 중복을 두 겹으로 막는다. 사전 검사만으로는 부족하다. 두 요청이 나란히
     * "없음"을 확인하고 둘 다 INSERT 하는 창이 남기 때문이다. 그 경합에서 뚫린 요청은
     * UNIQUE 제약(V12)에 걸려 {@link DuplicateKeyException} 으로 오는데, 그대로 두면
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

    @Override
    public LoginUser login(UserLoginRequest request) {

        User user = authMapper.findByEmail(request.getEmail());

        if (user == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return LoginUser.from(user);
    }

    @Override
    public UserMyPageResponse getMyPage(Long userId) {

        User user = authMapper.findById(userId);

        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return  UserMyPageResponse.from(user);
    }
}
