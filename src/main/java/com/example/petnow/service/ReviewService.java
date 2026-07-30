package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Review;
import com.example.petnow.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    // 리뷰 작성
    public void insertReview(ReviewCreateRequest request) {
        Review review = Review.builder()
                .score(request.getScore())
                .comment(request.getComment())
                .reservationId(request.getReservationId())
                .build();

        reviewMapper.insertReview(review);
    }


    // 내가 작성한 리뷰 조회
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        return reviewMapper.findReviewsByUser(userId);
    }

    // 특정 숙소 리뷰 조회
    public List<ReviewResponse> getReviewsByPlace(Long placeId) {
        return reviewMapper.findReviewsByPlace(placeId);
    }
}