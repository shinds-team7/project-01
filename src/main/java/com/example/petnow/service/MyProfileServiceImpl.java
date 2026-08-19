package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.common.storage.ImageCategory;
import com.example.petnow.dto.request.PasswordChangeRequest;
import com.example.petnow.dto.request.ProfileEditRequest;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.entity.User;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.mapper.MyProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MyProfileServiceImpl implements MyProfileService {

    private final MyProfileMapper myProfileMapper;
    private final FileStorage fileStorage;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public MyProfileResponse getProfile(Long userId) {
        return MyProfileResponse.from(findUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(Long userId, String nickname) {
        String normalizedNickname = normalizeNickname(nickname);
        return !normalizedNickname.isEmpty()
                && !myProfileMapper.existsActiveByNicknameExcludingUser(userId, normalizedNickname);
    }

    @Override
    @Transactional
    public MyProfileResponse updateProfile(Long userId, ProfileEditRequest request) {
        User currentUser = findUser(userId);
        String nickname = normalizeNickname(request.getNickname());
        if (myProfileMapper.existsActiveByNicknameExcludingUser(userId, nickname)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        String profileImageUrl = currentUser.getProfileImageUrl();
        MultipartFile image = request.getImage();
        boolean replacesImage = image != null && !image.isEmpty();
        if (replacesImage) {
            profileImageUrl = fileStorage.uploadImage(image, ImageCategory.USER);
        }

        int updatedRows = myProfileMapper.updateProfile(
                userId,
                nickname,
                normalizePhone(request.getPhone()),
                profileImageUrl);
        if (updatedRows == 0) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        if (replacesImage) {
            fileStorage.deleteImage(currentUser.getProfileImageUrl());
        }

        return MyProfileResponse.from(findUser(userId));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findUser(userId);
        if (!StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_CURRENT_PASSWORD);
        }

        int updatedRows = myProfileMapper.updatePassword(
                userId,
                passwordEncoder.encode(request.getNewPassword()));
        if (updatedRows == 0) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        int updatedRows = myProfileMapper.withdraw(userId);
        if (updatedRows == 0) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    private User findUser(Long userId) {
        User user = myProfileMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }

    private String normalizePhone(String phone) {
        return StringUtils.hasText(phone) ? phone.trim() : null;
    }
}
