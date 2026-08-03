package com.example.petnow.controller;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<Void> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        reviewService.insertReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}