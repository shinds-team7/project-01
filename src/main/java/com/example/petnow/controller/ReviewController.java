package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReviewResponse;
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

    /**
     * 세션에서 로그인 유저 id를 꺼내오는 헬퍼.
     * 다른 컨트롤러에서도
     * (Long) session.getAttribute(SessionConst.LOGIN_USER_ID) 를 반복 사용하므로
     * 공통 클래스로 빼면 좋을 듯
     */
    private Long getLoginUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
    }

    /**
     * 리뷰 작성 폼 화면
     * GET /reviews/new?reservationId=1
     * -> templates/reviews/form.html
     */
    @GetMapping("/new")
    public String reviewForm(@RequestParam Long reservationId, Model model,  HttpSession session) {
        // 로그인 안 되어 있으면 "redirect:/" 리턴
        Long loginUserId = getLoginUserId(session);
        if (loginUserId == null) {
            return "redirect:/";
        }

        ReviewCreateRequest form = new ReviewCreateRequest();
        form.setReservationId(reservationId);
        model.addAttribute("form", form);

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
                               HttpSession session) {
        if (bindingResult.hasErrors()) {
            // 검증 실패 시 다시 작성 폼으로
            return "reviews/create";     // 테스트용.
        }

        // 로그인 안 되어 있으면 "redirect:/" 리턴
        Long loginUserId = getLoginUserId(session);
        if (loginUserId == null) {
            return "redirect:/";
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
