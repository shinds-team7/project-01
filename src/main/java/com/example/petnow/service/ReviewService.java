package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.entity.Review;
import com.example.petnow.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    @Transactional
    public void insertReview(ReviewCreateRequest request) {
        Review review = Review.builder()
                .rating(request.getRating())
                .content(request.getContent())
                .reservationId(request.getReservationId())
                .build();

        reviewMapper.insertReview(review);
    }
}