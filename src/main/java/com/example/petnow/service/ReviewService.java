package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.request.ReviewReplyRequest;
import com.example.petnow.dto.request.ReviewUpdateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.ReviewSortType;

import java.util.List;

public interface ReviewService {
    // 리뷰 작성
    Long createReview(Long memberId, ReviewCreateRequest request);

    // 내가 작성한 리뷰 목록 조회
    List<ReviewResponse> getMyReviews(Long memberId);

    // 특정 장소의 리뷰 목록 조회
    List<ReviewResponse> getReviewsByPlace(Long placeId, ReviewSortType sort);

    // 내가 호스팅하는 장소들에 달린 리뷰 목록 조회 (호스트 홈 리뷰 탭)
    List<ReviewResponse> getReviewsForHost(Long hostUserId);

    // 리뷰 단건 조회
    ReviewResponse getReview(Long memberId, Long reviewId);

    // 리뷰 수정
    void updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request);

    // 리뷰 삭제
    void deleteReview(Long memberId, Long reviewId);

    // 호스트 답글 작성·수정 (리뷰 하나에 답글은 최대 1개라 이미 있으면 덮어쓴다)
    void saveReply(Long hostUserId, Long placeId, Long reviewId, ReviewReplyRequest request);

    // 호스트 답글 삭제
    void deleteReply(Long hostUserId, Long placeId, Long reviewId);
}
