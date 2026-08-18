package com.example.petnow.service;

import com.example.petnow.dto.request.PasswordChangeRequest;
import com.example.petnow.dto.request.ProfileEditRequest;
import com.example.petnow.dto.response.MyProfileResponse;

public interface MyProfileService {

    MyProfileResponse getProfile(Long userId);

    boolean isNicknameAvailable(Long userId, String nickname);

    MyProfileResponse updateProfile(Long userId, ProfileEditRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    void withdraw(Long userId);

}
