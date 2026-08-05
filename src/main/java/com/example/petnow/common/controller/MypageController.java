package com.example.petnow.common.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.service.PetService;
import com.example.petnow.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final PetService petService;
    private final UserService userService;

    @GetMapping("/mypage")
    public String mypage(Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
        model.addAttribute("petList", loginUserId != null ? petService.getPetList(loginUserId) : java.util.List.of());
        model.addAttribute("user", loginUserId != null ? userService.getMyPage(loginUserId) : null);
        return "mypage";
    }

    /**
     * 예약 상세. 아직 예약 조회 API가 없어 마이페이지의 데모 예약 카드와
     * 같은 고정 데이터를 보여준다.
     */
    public record DemoBooking(
            Long id, String placeName, String region, String hostName, String status, String statusColor,
            String whenLabel, String petLine, String reservationCode, String paidAt, String payMethod,
            int usageFee, int serviceFee, boolean cancellable) {}

    private static final Map<Long, DemoBooking> BOOKINGS = new LinkedHashMap<>();

    static {
        BOOKINGS.put(1L, new DemoBooking(1L, "초코의 포근한 오후", "서울 마포구 연남동", "윤슬 호스트",
                "확정", "var(--pet-green-dark)", "2026.08.08 (토) 14:00 ~ 20:00", "초코 · 토이 푸들 · 4.2kg",
                "PN-20260808-0001", "2026.08.07 (금) 21:04", "토스페이", 60000, 3000, true));
        BOOKINGS.put(2L, new DemoBooking(2L, "보리의 주말 산책 돌봄", "서울 성동구 성수동", "성수 조용한 단독주택",
                "승인 대기", "#E0A21C", "2026.08.21 (금) 10:00 ~ 17:00", "보리 · 비글 · 11kg",
                "PN-20260821-0002", "2026.08.20 (목) 09:12", "신용카드", 49000, 2500, true));
        BOOKINGS.put(3L, new DemoBooking(3L, "모카의 넓은 거실 하루", "서울 송파구 잠실동", "넓은 거실 아파트",
                "이용 완료", "var(--muted)", "2026.07.18 (금) ~ 2026.07.19 (토) · 1박", "모카 · 말티즈 · 4kg",
                "PN-20260718-0003", "2026.07.17 (목) 18:30", "토스페이", 79000, 3000, false));
    }

    @GetMapping("/mypage/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        model.addAttribute("booking", BOOKINGS.getOrDefault(id, BOOKINGS.get(1L)));
        return "booking-detail";
    }
}
