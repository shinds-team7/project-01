package com.example.petnow.service;

import com.example.petnow.dto.UserSignupRequest;
import com.example.petnow.entity.User;
import com.example.petnow.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public void signup(UserSignupRequest request) {

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(request.getPassword())
                .build();

        userMapper.signup(user);
    }
}
