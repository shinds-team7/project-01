package com.example.petnow.service;

import com.example.petnow.dto.ReviewCreateRequest;
import com.example.petnow.entity.Review;
import com.example.petnow.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public void insertReview(ReviewCreateRequest request) {
        Review review = Review.builder()
                .score(request.getScore())
                .comment(request.getComment())
                .reservationId(request.getReservationId())
                .build();

        reviewMapper.insertReview(review);
    }
}