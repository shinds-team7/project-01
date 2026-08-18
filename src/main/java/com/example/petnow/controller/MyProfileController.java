package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.PasswordChangeRequest;
import com.example.petnow.dto.request.ProfileEditRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.dto.response.NicknameAvailabilityResponse;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.service.MyProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyProfileController {

    private static final int MAX_NICKNAME_LENGTH = 20;

    private final MyProfileService myProfileService;

    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/auth/login";
        }

        addProfileModel(userId, model);
        return "mypage/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/auth/login";
        }

        MyProfileResponse profile = addProfileModel(userId, model);
        ProfileEditRequest request = new ProfileEditRequest();
        request.setNickname(profile.getNickname());
        request.setPhone(profile.getPhone());
        model.addAttribute("profileEditRequest", request);
        return "mypage/profileEdit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute ProfileEditRequest profileEditRequest,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            addProfileModel(userId, model);
            return "mypage/profileEdit";
        }

        MyProfileResponse updatedProfile;
        try {
            updatedProfile = myProfileService.updateProfile(userId, profileEditRequest);
        } catch (BusinessException e) {
            if (e.getErrorCode() != UserErrorCode.DUPLICATE_NICKNAME) {
                throw e;
            }
            bindingResult.rejectValue("nickname", "duplicateNickname", e.getMessage());
            addProfileModel(userId, model);
            return "mypage/profileEdit";
        }

        LoginSession.set(session, LoginUser.builder()
                .id(userId)
                .nickname(updatedProfile.getNickname())
                .email(updatedProfile.getEmail())
                .build());
        redirectAttributes.addFlashAttribute("profileUpdated", true);
        return "redirect:/my/profile";
    }

    @GetMapping("/profile/nickname-availability")
    @ResponseBody
    public ResponseEntity<NicknameAvailabilityResponse> nicknameAvailability(
            @RequestParam String nickname,
            HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String normalizedNickname = nickname.trim();
        if (normalizedNickname.isEmpty()) {
            return ResponseEntity.ok(new NicknameAvailabilityResponse(false, "닉네임을 입력해주세요."));
        }
        if (normalizedNickname.length() > MAX_NICKNAME_LENGTH) {
            return ResponseEntity.ok(new NicknameAvailabilityResponse(false, "닉네임은 20자 이하로 입력해주세요."));
        }

        boolean available = myProfileService.isNicknameAvailable(userId, normalizedNickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ResponseEntity.ok(new NicknameAvailabilityResponse(available, message));
    }

    @GetMapping("/password")
    public String passwordForm(@ModelAttribute PasswordChangeRequest passwordChangeRequest,
                               HttpSession session) {
        if (getLoginUserId(session) == null) {
            return "redirect:/auth/login";
        }
        return "mypage/passwordEdit";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeRequest passwordChangeRequest,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            return "mypage/passwordEdit";
        }
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getNewPasswordConfirm())) {
            bindingResult.rejectValue(
                    "newPasswordConfirm",
                    "passwordMismatch",
                    "새 비밀번호가 일치하지 않습니다.");
            return "mypage/passwordEdit";
        }

        try {
            myProfileService.changePassword(userId, passwordChangeRequest);
        } catch (BusinessException e) {
            if (e.getErrorCode() != UserErrorCode.INVALID_CURRENT_PASSWORD) {
                throw e;
            }
            bindingResult.rejectValue("currentPassword", "invalidCurrentPassword", e.getMessage());
            return "mypage/passwordEdit";
        }

        redirectAttributes.addFlashAttribute("passwordUpdated", true);
        return "redirect:/my/profile";
    }

    @PostMapping("/withdraw")
    public String withdraw(HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/auth/login";
        }

        myProfileService.withdraw(userId);
        LoginSession.clear(session);
        return "redirect:/home";
    }

    private MyProfileResponse addProfileModel(Long userId, Model model) {
        MyProfileResponse profile = myProfileService.getProfile(userId);
        model.addAttribute("profile", profile);
        model.addAttribute("profileImageUrl", profile.getProfileImageUrl());
        return profile;
    }

    private Long getLoginUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
    }
}
