package com.example.petnow.controller;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성 폼
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false, defaultValue = "성수 조용한 단독주택 마당") String placeName,
            @RequestParam(required = false, defaultValue = "7월 18일 · 1박 · 초코") String stayInfo,
            Model model) {
        ReviewCreateRequest reviewCreateRequest = new ReviewCreateRequest();
        reviewCreateRequest.setReservationId(reservationId);
        model.addAttribute("reviewCreateRequest", reviewCreateRequest);
        model.addAttribute("placeName", placeName);
        model.addAttribute("stayInfo", stayInfo);
        return "reviews/create";
    }

    // 리뷰 등록
    @PostMapping
    public String createReview(
            @Valid @ModelAttribute ReviewCreateRequest request,
            BindingResult bindingResult,
            @RequestParam(required = false, defaultValue = "성수 조용한 단독주택 마당") String placeName,
            @RequestParam(required = false, defaultValue = "7월 18일 · 1박 · 초코") String stayInfo,
            Model model) {

        if (bindingResult.hasErrors()) {
            // 검증 실패 시 다시 작성 폼으로
            model.addAttribute("placeName", placeName);
            model.addAttribute("stayInfo", stayInfo);
            return "reviews/create";
        }

        try {
            reviewService.insertReview(request);
        } catch (DataIntegrityViolationException e) {
            // 예약 조회 API가 아직 없어 존재하지 않는 예약 번호로 리뷰를 시도한 경우
            model.addAttribute("placeName", placeName);
            model.addAttribute("stayInfo", stayInfo);
            model.addAttribute("submitError", "연결된 예약 정보를 찾을 수 없어요. 예약 내역에서 다시 시도해주세요.");
            return "reviews/create";
        }
        return "redirect:/mypage?reviewed=1#reservations";
    }
}
