package com.example.petnow.service;

import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.entity.User;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
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
                throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
            }

        return MyProfileResponse.from(user);

        }
    }
