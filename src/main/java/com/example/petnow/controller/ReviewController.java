package com.example.petnow.controller;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.request.ReviewUpdateRequest;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.ReviewSortType;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReservationService reservationService;

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
    public String reviewForm(@RequestParam Long reservationId, Model model, @LoginUser Long loginUserId) {

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
    public String createReview(@LoginUser Long loginUserId,
                               @ModelAttribute("form") @Valid ReviewCreateRequest request,
                               BindingResult bindingResult,
                               Model model) {

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
    public String myReviews(Model model, @LoginUser Long loginUserId) {

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
    public String reviewsByPlace(@PathVariable Long placeId,
                                 @RequestParam(defaultValue = "LATEST") ReviewSortType sort,
                                 Model model) {
        List<ReviewResponse> reviews = reviewService.getReviewsByPlace(placeId, sort);
        model.addAttribute("placeId", placeId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("currentSort", sort);
        return "reviews/list";     // 테스트용. 장소별 리뷰 목록으로 바꿔야함
    }

    /**
     * 리뷰 수정 폼 화면
     * GET /reviews/{id}/edit
     * -> templates/reviews/edit.html
     */
    @GetMapping("/{reviewId}/edit")
    public String editForm(@PathVariable Long reviewId, Model model, @LoginUser Long loginUserId) {

        ReviewResponse review = reviewService.getReview(loginUserId, reviewId);

        // 폼 바인딩용 객체를 기존 값으로 채워서 넘긴다 (create 화면과 동일하게 "form")
        ReviewUpdateRequest form = new ReviewUpdateRequest();
        form.setRating(review.getRating());
        form.setContent(review.getContent());
        model.addAttribute("form", form);

        // 상단 요약 카드용 (create 화면과 같은 키를 쓴다)
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("placeName", review.getPlaceName());
        model.addAttribute("checkInAt", review.getCheckInAt());

        return "reviews/edit";
    }

    /**
     * 리뷰 수정 처리
     * POST /reviews/{id}
     * 성공 시 내 리뷰 목록 페이지로 redirect
     */
    @PostMapping("/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                               @ModelAttribute("form") @Valid ReviewUpdateRequest request,
                               BindingResult bindingResult,
                               Model model,
                               @LoginUser Long loginUserId) {

        if (bindingResult.hasErrors()) {
            ReviewResponse review = reviewService.getReview(loginUserId, reviewId);
            model.addAttribute("reviewId", reviewId);
            model.addAttribute("placeName", review.getPlaceName());
            model.addAttribute("checkInAt", review.getCheckInAt());
            return "reviews/edit";     // 테스트용.
        }

        reviewService.updateReview(loginUserId, reviewId, request);

        return "redirect:/reviews/my";
    }

    /**
     * 리뷰 삭제 처리
     * POST /reviews/{reviewId}/delete
     * 성공 시 내 리뷰 목록 페이지로 redirect
     */
    @PostMapping("/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId, @LoginUser Long loginUserId) {

        reviewService.deleteReview(loginUserId, reviewId);

        return "redirect:/reviews/my";
    }
}
