package com.example.petnow.service;

import com.example.petnow.dto.response.MyProfileResponse;

public interface MyProfileService {

    MyProfileResponse getProfile(Long userId);

}
