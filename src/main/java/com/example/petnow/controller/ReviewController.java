package com.example.petnow.controller;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성 폼
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reviewCreateRequest", new ReviewCreateRequest());
        return "reviews/create";
    }

    // 리뷰 등록
    @PostMapping
    public String createReview(
            @Valid @ModelAttribute ReviewCreateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            // 검증 실패 시 다시 작성 폼으로
            return "reviews/create";
        }

        reviewService.insertReview(request);
        return "redirect:/reviews"; // 저장 후 리다이렉트 (예약 내역 페이지로 바꿔야함)
    }
}
