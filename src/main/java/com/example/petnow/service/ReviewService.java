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
        // 요청 DTO 필드명 -> ERD 컬럼명
        Review review = Review.builder()
                .rating(request.getScore())
                .content(request.getComment())
                .reservationId(request.getReservationId())
                .build();

        reviewMapper.insertReview(review);
    }
}