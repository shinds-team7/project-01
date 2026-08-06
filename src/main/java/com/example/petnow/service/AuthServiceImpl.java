package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.entity.User;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signup(UserSignupRequest request) {

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userMapper.signup(user);
    }

    @Override
    public Long login(UserLoginRequest request) {

        User user = userMapper.findByEmail(request.getEmail());

        if (user == null) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return user.getId();
    }

    @Override
    public UserMyPageResponse getMyPage(Long userId) {

        User user = userMapper.findById(userId);

        return  UserMyPageResponse.from(user);
    }
}
