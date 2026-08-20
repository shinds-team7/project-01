package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.common.session.LoginSession;
import com.example.petnow.dto.request.PasswordChangeRequest;
import com.example.petnow.dto.request.ProfileEditRequest;
import com.example.petnow.dto.response.MyProfileResponse;
import com.example.petnow.dto.response.NicknameAvailabilityResponse;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.service.MyProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * 마이페이지의 내 정보 화면.
 *
 * <p>{@code /my/**} 는 {@code WebConfig} 의 보호 목록에 있어 비로그인 요청은 여기까지 오지 않는다.
 * 그래서 세션을 직접 확인하지 않고 {@link LoginUser} 로 사용자 id 만 받는다.
 */
@Controller
@RequestMapping("/my")
@RequiredArgsConstructor
public class MyProfileController {

    private static final int MAX_NICKNAME_LENGTH = 20;

    private final MyProfileService myProfileService;

    // 프로필 상세 조회
    @GetMapping("/profile")
    public String getProfile(@LoginUser Long loginUserId, Model model) {

        addProfileModel(loginUserId, model);

        return "mypage/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(@LoginUser Long loginUserId, Model model) {

        MyProfileResponse profile = addProfileModel(loginUserId, model);

        ProfileEditRequest request = new ProfileEditRequest();
        request.setNickname(profile.getNickname());
        request.setPhone(profile.getPhone());
        model.addAttribute("profileEditRequest", request);

        return "mypage/profileEdit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute ProfileEditRequest profileEditRequest,
                                BindingResult bindingResult,
                                @LoginUser Long loginUserId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addProfileModel(loginUserId, model);
            return "mypage/profileEdit";
        }

        MyProfileResponse updatedProfile;
        try {
            updatedProfile = myProfileService.updateProfile(loginUserId, profileEditRequest);
        } catch (BusinessException e) {
            if (e.getErrorCode() != UserErrorCode.DUPLICATE_NICKNAME) {
                throw e;
            }
            bindingResult.rejectValue("nickname", "duplicateNickname", e.getMessage());
            addProfileModel(loginUserId, model);
            return "mypage/profileEdit";
        }

        // 화면에 그려지는 표시용 값은 로그인 시점 스냅샷이라, 저장 직후 다시 채워 넣어야 낡지 않는다.
        LoginSession.set(session, com.example.petnow.dto.response.LoginUser.builder()
                .id(loginUserId)
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
            @LoginUser Long loginUserId) {

        String normalizedNickname = nickname.trim();
        if (normalizedNickname.isEmpty()) {
            return ResponseEntity.ok(new NicknameAvailabilityResponse(false, "닉네임을 입력해주세요."));
        }
        if (normalizedNickname.length() > MAX_NICKNAME_LENGTH) {
            return ResponseEntity.ok(new NicknameAvailabilityResponse(false, "닉네임은 20자 이하로 입력해주세요."));
        }

        boolean available = myProfileService.isNicknameAvailable(loginUserId, normalizedNickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ResponseEntity.ok(new NicknameAvailabilityResponse(available, message));
    }

    @GetMapping("/password")
    public String passwordForm(@ModelAttribute PasswordChangeRequest passwordChangeRequest,
                               @LoginUser Long loginUserId,
                               RedirectAttributes redirectAttributes) {
        if (myProfileService.getProfile(loginUserId).isKakaoUser()) {
            redirectAttributes.addFlashAttribute("socialPasswordBlocked", true);
            return "redirect:/my/profile";
        }
        return "mypage/passwordEdit";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeRequest passwordChangeRequest,
                                 BindingResult bindingResult,
                                 @LoginUser Long loginUserId,
                                 RedirectAttributes redirectAttributes) {
        if (myProfileService.getProfile(loginUserId).isKakaoUser()) {
            redirectAttributes.addFlashAttribute("socialPasswordBlocked", true);
            return "redirect:/my/profile";
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
            myProfileService.changePassword(loginUserId, passwordChangeRequest);
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
    public String withdraw(@LoginUser Long loginUserId, HttpSession session) {

        myProfileService.withdraw(loginUserId);

        LoginSession.clear(session);
        return "redirect:/home";
    }

    private MyProfileResponse addProfileModel(Long loginUserId, Model model) {
        MyProfileResponse profile = myProfileService.getProfile(loginUserId);
        model.addAttribute("profile", profile);
        model.addAttribute("profileImageUrl", profile.getProfileImageUrl());
        return profile;
    }
}
