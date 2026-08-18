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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class MyProfileServiceImplTest {

    private MyProfileMapper myProfileMapper;
    private FileStorage fileStorage;
    private PasswordEncoder passwordEncoder;
    private MyProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        myProfileMapper = mock(MyProfileMapper.class);
        fileStorage = mock(FileStorage.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new MyProfileServiceImpl(myProfileMapper, fileStorage, passwordEncoder);
    }

    @Test
    @DisplayName("프로필 이미지는 새 파일 업로드와 DB 갱신 뒤 기존 파일을 삭제한다")
    void updateProfileReplacesImageInSafeOrder() {
        User current = user("초코", "stored-hash", "https://old/image.png");
        User updated = user("새초코", "stored-hash", "https://new/image.png");
        given(myProfileMapper.findById(7L)).willReturn(current, updated);
        given(myProfileMapper.existsActiveByNicknameExcludingUser(7L, "새초코")).willReturn(false);
        given(fileStorage.uploadImage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(ImageCategory.USER)))
                .willReturn("https://new/image.png");
        given(myProfileMapper.updateProfile(7L, "새초코", null, "https://new/image.png")).willReturn(1);

        ProfileEditRequest request = new ProfileEditRequest();
        request.setNickname(" 새초코 ");
        request.setPhone(" ");
        request.setImage(new MockMultipartFile("image", "new.png", "image/png", new byte[]{1}));

        MyProfileResponse result = service.updateProfile(7L, request);

        assertThat(result.getNickname()).isEqualTo("새초코");
        then(fileStorage).should().uploadImage(request.getImage(), ImageCategory.USER);
        then(myProfileMapper).should().updateProfile(7L, "새초코", null, "https://new/image.png");
        then(fileStorage).should().deleteImage("https://old/image.png");
    }

    @Test
    @DisplayName("이미 쓰이는 닉네임이면 이미지 업로드와 DB 갱신 전에 막는다")
    void duplicateNicknameStopsUpdate() {
        User current = user("초코", "stored-hash", null);
        given(myProfileMapper.findById(7L)).willReturn(current);
        given(myProfileMapper.existsActiveByNicknameExcludingUser(7L, "중복닉네임")).willReturn(true);

        ProfileEditRequest request = new ProfileEditRequest();
        request.setNickname("중복닉네임");

        assertThatThrownBy(() -> service.updateProfile(7L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.DUPLICATE_NICKNAME);

        then(fileStorage).shouldHaveNoInteractions();
        then(myProfileMapper).should(never()).updateProfile(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.nullable(String.class));
    }

    @Test
    @DisplayName("현재 사용자의 기존 닉네임은 중복으로 보지 않는다")
    void currentNicknameIsAvailable() {
        given(myProfileMapper.existsActiveByNicknameExcludingUser(7L, "초코")).willReturn(false);

        assertThat(service.isNicknameAvailable(7L, " 초코 ")).isTrue();
    }

    @Test
    @DisplayName("현재 비밀번호가 맞으면 새 비밀번호를 해시해 저장한다")
    void changePasswordEncodesNewPassword() {
        User current = user("초코", "stored-hash", null);
        given(myProfileMapper.findById(7L)).willReturn(current);
        given(passwordEncoder.matches("oldPassword1", "stored-hash")).willReturn(true);
        given(passwordEncoder.encode("newPassword1")).willReturn("new-hash");
        given(myProfileMapper.updatePassword(7L, "new-hash")).willReturn(1);

        service.changePassword(7L, passwordRequest("oldPassword1", "newPassword1"));

        then(myProfileMapper).should().updatePassword(7L, "new-hash");
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 새 비밀번호를 저장하지 않는다")
    void invalidCurrentPasswordStopsUpdate() {
        User current = user("초코", "stored-hash", null);
        given(myProfileMapper.findById(7L)).willReturn(current);
        given(passwordEncoder.matches("wrongPassword1", "stored-hash")).willReturn(false);

        assertThatThrownBy(() -> service.changePassword(7L, passwordRequest("wrongPassword1", "newPassword1")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_CURRENT_PASSWORD);

        then(myProfileMapper).should(never()).updatePassword(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private PasswordChangeRequest passwordRequest(String currentPassword, String newPassword) {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        request.setNewPasswordConfirm(newPassword);
        return request;
    }

    private User user(String nickname, String password, String profileImageUrl) {
        User user = mock(User.class);
        given(user.getNickname()).willReturn(nickname);
        given(user.getEmail()).willReturn("choco@petnow.kr");
        given(user.getPhone()).willReturn("010-1111-2222");
        given(user.getPassword()).willReturn(password);
        given(user.getProfileImageUrl()).willReturn(profileImageUrl);
        return user;
    }
}
