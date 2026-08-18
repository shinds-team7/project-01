package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.PasswordChangeRequest;
import com.example.petnow.dto.request.ProfileEditRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.service.MyProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MyProfileController.class)
class MyProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyProfileService myProfileService;

    @Test
    @DisplayName("프로필 수정 화면이 현재 값과 이미지 URL을 모델에 채운다")
    void editProfileFormProvidesServerContract() throws Exception {
        given(myProfileService.getProfile(7L)).willReturn(profile("초코", "/uploads/choco.png"));

        mockMvc.perform(get("/my/profile/edit").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/profileEdit"))
                .andExpect(model().attributeExists("profile", "profileEditRequest"))
                .andExpect(model().attribute("profileImageUrl", "/uploads/choco.png"));
    }

    @Test
    @DisplayName("닉네임 중복 확인이 boolean과 메시지를 JSON으로 돌려준다")
    void nicknameAvailabilityReturnsJsonContract() throws Exception {
        given(myProfileService.isNicknameAvailable(7L, "새닉네임")).willReturn(true);

        mockMvc.perform(get("/my/profile/nickname-availability")
                        .param("nickname", " 새닉네임 ")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."));
    }

    @Test
    @DisplayName("프로필 수정 성공 시 세션 닉네임을 갱신하고 성공 배너로 리다이렉트한다")
    void updateProfileRefreshesSession() throws Exception {
        MockHttpSession session = loggedIn();
        given(myProfileService.updateProfile(eq(7L), any(ProfileEditRequest.class)))
                .willReturn(profile("새초코", "/uploads/new.png"));

        mockMvc.perform(multipart("/my/profile/edit")
                        .param("nickname", "새초코")
                        .param("phone", "010-1111-2222")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my/profile"))
                .andExpect(flash().attribute("profileUpdated", true));

        LoginUser loginUser = (LoginUser) session.getAttribute(SessionConst.LOGIN_USER);
        assertThat(loginUser.getNickname()).isEqualTo("새초코");
        assertThat(loginUser.getEmail()).isEqualTo("choco@petnow.kr");
    }

    @Test
    @DisplayName("중복 닉네임이면 오류 페이지가 아니라 수정 폼의 필드 오류로 표시한다")
    void duplicateNicknameRerendersEditForm() throws Exception {
        given(myProfileService.updateProfile(eq(7L), any(ProfileEditRequest.class)))
                .willThrow(new BusinessException(UserErrorCode.DUPLICATE_NICKNAME));
        given(myProfileService.getProfile(7L)).willReturn(profile("초코", null));

        mockMvc.perform(multipart("/my/profile/edit")
                        .param("nickname", "중복닉네임")
                        .param("phone", "")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/profileEdit"))
                .andExpect(model().attributeHasFieldErrors("profileEditRequest", "nickname"));
    }

    @Test
    @DisplayName("새 비밀번호 확인이 다르면 서비스 호출 없이 폼을 다시 그린다")
    void passwordMismatchRerendersForm() throws Exception {
        mockMvc.perform(post("/my/password")
                        .param("currentPassword", "oldPassword1")
                        .param("newPassword", "newPassword1")
                        .param("newPasswordConfirm", "different1")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/passwordEdit"))
                .andExpect(model().attributeHasFieldErrors("passwordChangeRequest", "newPasswordConfirm"))
                .andExpect(content().string(not(containsString("oldPassword1"))))
                .andExpect(content().string(not(containsString("newPassword1"))));

        then(myProfileService).should(never()).changePassword(eq(7L), any(PasswordChangeRequest.class));
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 해당 필드 오류로 표시한다")
    void invalidCurrentPasswordRerendersForm() throws Exception {
        willThrow(new BusinessException(UserErrorCode.INVALID_CURRENT_PASSWORD))
                .given(myProfileService).changePassword(eq(7L), any(PasswordChangeRequest.class));

        mockMvc.perform(post("/my/password")
                        .param("currentPassword", "wrongPassword1")
                        .param("newPassword", "newPassword1")
                        .param("newPasswordConfirm", "newPassword1")
                        .session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/passwordEdit"))
                .andExpect(model().attributeHasFieldErrors("passwordChangeRequest", "currentPassword"));
    }

    @Test
    @DisplayName("비밀번호 변경 성공 시 성공 배너로 리다이렉트한다")
    void passwordChangeRedirectsWithBanner() throws Exception {
        mockMvc.perform(post("/my/password")
                        .param("currentPassword", "oldPassword1")
                        .param("newPassword", "newPassword1")
                        .param("newPasswordConfirm", "newPassword1")
                        .session(loggedIn()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my/profile"))
                .andExpect(flash().attribute("passwordUpdated", true));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 프로필 수정 화면 대신 로그인으로 이동한다")
    void editProfileRequiresLogin() throws Exception {
        mockMvc.perform(get("/my/profile/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    private MyProfileResponse profile(String nickname, String profileImageUrl) {
        return MyProfileResponse.builder()
                .nickname(nickname)
                .email("choco@petnow.kr")
                .phone("010-1111-2222")
                .profileImageUrl(profileImageUrl)
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 7L);
        session.setAttribute(SessionConst.LOGIN_USER, LoginUser.builder()
                .id(7L)
                .nickname("초코")
                .email("choco@petnow.kr")
                .build());
        return session;
    }
}
