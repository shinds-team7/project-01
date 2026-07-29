package com.example.petnow.controller;

import com.example.petnow.dto.ReviewCreateRequest;
import com.example.petnow.entity.Review;
import com.example.petnow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/write")
    public ResponseEntity<Void> createReview(@RequestBody ReviewCreateRequest request) {
        reviewService.insertReview(request);
        return ResponseEntity.ok().build();
    }
}