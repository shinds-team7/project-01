package com.example.petnow.service;

import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.entity.User;
import com.example.petnow.mapper.MyProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyProfileServiceImpl implements MyProfileService {

        private final MyProfileMapper myProfileMapper;

        @Override
        public MyProfileResponse getProfile(Long userId) {

            User user = myProfileMapper.findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
            }

        return MyProfileResponse.from(user);

        }
    }
