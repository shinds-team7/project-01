package com.example.petnow.common.controller;

import com.example.petnow.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 호스트 대시보드. "내 호스팅" 탭은 PlaceService로 실제 등록된 장소를 보여주고,
 * "예약 관리"/"리뷰 관리" 탭은 아직 예약·리뷰 조회 API가 없어 데모 데이터로 화면만 구성한다.
 */
@Controller
@RequiredArgsConstructor
public class HostController {

    private final PlaceService placeService;

    @GetMapping("/host")
    public String dashboard(@RequestParam(defaultValue = "places") String tab, Model model, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("loginUserId");
        if (loginUserId == null) {
            return "redirect:/";
        }

        model.addAttribute("tab", tab);
        model.addAttribute("places", placeService.getPlacesByUserId(loginUserId));
        model.addAttribute("bookings", List.copyOf(HOST_BOOKINGS.values()));
        return "host/dashboard";
    }

    public record DemoHostBooking(
            Long id, String place, String when, String guestName, String petLine,
            String status, boolean waiting, int estimatedPayout) {}

    private static final Map<Long, DemoHostBooking> HOST_BOOKINGS = new LinkedHashMap<>();

    static {
        HOST_BOOKINGS.put(1L, new DemoHostBooking(1L, "성수 단독주택 마당", "2026.08.09 (일) 13:00~18:00",
                "김하늘", "초코 · 푸들 · 6kg · 소형 · 중성화 완료", "승인 대기", true, 57000));
        HOST_BOOKINGS.put(2L, new DemoHostBooking(2L, "성수 단독주택 마당", "2026.08.15 (토) 10:00~16:00",
                "이도현", "보리 · 비글 · 11kg · 중형", "승인 완료", false, 57000));
    }

    @GetMapping("/host/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("loginUserId") == null) {
            return "redirect:/";
        }
        model.addAttribute("booking", HOST_BOOKINGS.getOrDefault(id, HOST_BOOKINGS.get(1L)));
        return "host/booking-detail";
    }
}
