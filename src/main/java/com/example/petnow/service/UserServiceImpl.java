package com.example.petnow.service;

import com.example.petnow.dto.request.UserLoginRequest;
import com.example.petnow.dto.request.UserSignupRequest;
import com.example.petnow.entity.User;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ErrorCode;
import com.example.petnow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
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
    public void login(UserLoginRequest request) {

        User user = userMapper.findByEmail(request.getEmail());

        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
