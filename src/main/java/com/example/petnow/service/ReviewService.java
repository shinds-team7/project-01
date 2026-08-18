package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    // 리뷰 작성
    Long createReview(Long memberId, ReviewCreateRequest request);

    // 내가 작성한 리뷰 목록 조회
    List<ReviewResponse> getMyReviews(Long memberId);

    // 특정 장소의 리뷰 목록 조회
    List<ReviewResponse> getReviewsByPlace(Long placeId);


}
