package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.UserMyPageResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 마이페이지 화면 컨트롤러.
 *
 * <p>{@code /mypage} 를 담당하는 유일한 컨트롤러입니다. 예전에는
 * {@code common.controller.MypageController} 에도 같은 경로가 있었지만 매핑이 주석 처리된
 * 죽은 클래스였고, 그 탓에 디자인이 적용된 {@code mypage.html} 대신 표만 있는
 * {@code mypage/index.html} 이 렌더링됐습니다. 두 클래스를 이 하나로 합쳤습니다.
 *
 * <p>화면에 넘기는 값은 두 가지입니다.
 * <ul>
 *   <li>{@code user} — {@link UserMyPageResponse}. 닉네임과 이메일. 상단 인사말과 계정 요약에 씁니다.</li>
 *   <li>{@code petList} — {@code List<PetListResponse>}. 이름·크기·몸무게.
 *       "한눈에 보기" 탭의 반려동물 리본과 "반려동물" 탭의 카드 목록에 씁니다.</li>
 * </ul>
 *
 * <p>템플릿은 두 값이 모두 {@code null} 이거나 비어 있어도 깨지지 않고 안내 문구를 보여줍니다.
 * 예약·찜·리뷰 탭은 아직 API가 없어 화면상 예시 데이터로 남아 있습니다. 담당 기능이
 * 구현되면 이 메서드에 모델 속성을 추가하고 해당 탭을 바꿔주세요.
 */
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final PetService petService;
    private final UserService userService;

    @GetMapping
    public String myPage(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);

        // 비로그인 상태에서는 조회할 사용자가 없습니다. 가드가 없으면
        // UserMyPageResponse.from(null) 에서 NPE 가 나 500 이 됩니다.
        if (userId == null) {
            return "redirect:/";
        }

        model.addAttribute("petList", petService.getPetList(userId));

        UserMyPageResponse user = userService.getMyPage(userId);
        model.addAttribute("user", user);

        return "mypage";
    }
}
