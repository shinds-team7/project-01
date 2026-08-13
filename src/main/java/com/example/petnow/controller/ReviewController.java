package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReservationService reservationService;

    /**
     * 세션에서 로그인 유저 id를 꺼내오는 헬퍼.
     */
    private Long getLoginUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
    }

    /**
     * 작성 폼 상단 요약 카드(장소명 · 체크인 날짜)를 모델에 채운다.
     * 조회 과정에서 "본인 예약인가" 검증도 함께 이뤄진다.
     */
    private void addReservationSummary(Model model, Long reservationId, Long loginUserId) {
        if (reservationId == null) {   // reservationId 자체가 검증 실패한 경우
            return;
        }

        ReservationDetailResponse reservation =
                reservationService.detailReservation(reservationId, loginUserId);
        if (reservation == null) {   // 요약 카드는 없어도 작성 자체는 되게 둔다
            return;
        }

        model.addAttribute("placeName", reservation.getPlaceName());
        model.addAttribute("checkInAt",
                reservation.getCheckIn() == null ? null : reservation.getCheckIn().toLocalDate());
    }

    /**
     * 리뷰 작성 폼 화면
     * GET /reviews/new?reservationId=1
     * -> templates/reviews/form.html
     */
    @GetMapping("/new")
    public String reviewForm(@RequestParam Long reservationId, Model model,  HttpSession session) {
        Long loginUserId = getLoginUserId(session);
        if (loginUserId == null) {
            return "redirect:/";
        }

        ReviewCreateRequest form = new ReviewCreateRequest();
        form.setReservationId(reservationId);
        model.addAttribute("form", form);

        addReservationSummary(model, reservationId, loginUserId);

        return "reviews/create";     // 테스트용.
    }

    /**
     * 리뷰 작성 처리
     * POST /reviews
     * 성공 시 내 리뷰 목록 페이지로 redirect
     */
    @PostMapping
    public String createReview(@ModelAttribute("form") @Valid ReviewCreateRequest request,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session) {
        Long loginUserId = getLoginUserId(session);
        if (loginUserId == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            addReservationSummary(model, request.getReservationId(), loginUserId);
            // 검증 실패 시 다시 작성 폼으로
            return "reviews/create";     // 테스트용.
        }

        reviewService.createReview(loginUserId, request);

        return "redirect:/reviews/my";      // 테스트용. 내 리뷰 목록으로 바꿔야함
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     * GET /reviews/my
     * -> templates/review/my-list.html
     */
    @GetMapping("/my")
    public String myReviews(Model model, HttpSession session) {
        Long loginUserId = getLoginUserId(session);
        if (loginUserId == null) {
            return "redirect:/";
        }

        List<ReviewResponse> reviews = reviewService.getMyReviews(loginUserId);
        model.addAttribute("reviews", reviews);

        return "reviews/list";       // 테스트용. 내 리뷰 목록으로 바꿔야함
    }

    /**
     * 특정 장소의 리뷰 목록 조회
     * GET /reviews/place/{placeId}
     * -> templates/review/place-list.html
     */
    @GetMapping("/place/{placeId}")
    public String reviewsByPlace(@PathVariable Long placeId, Model model) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPlace(placeId);
        model.addAttribute("placeId", placeId);
        model.addAttribute("reviews", reviews);
        return "reviews/list";     // 테스트용. 장소별 리뷰 목록으로 바꿔야함
    }
}
