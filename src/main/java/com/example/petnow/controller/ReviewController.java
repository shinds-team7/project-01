package com.example.petnow.controller;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@PathVariable Long userId) {

        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @GetMapping("/places/{placeId}")
    public ResponseEntity<List<ReviewResponse>> getPlaceReviews(@PathVariable Long placeId) {

        return ResponseEntity.ok(reviewService.getReviewsByPlace(placeId));
    }

}