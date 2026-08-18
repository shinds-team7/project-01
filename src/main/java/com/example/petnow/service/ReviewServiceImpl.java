package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.request.ReviewUpdateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Review;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ReviewErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final PlaceMapper placeMapper;

    // 리뷰 작성
    @Transactional
    public Long createReview(Long memberId, ReviewCreateRequest request) {
        // 이 예약이 실제로 로그인한 회원 본인의 예약인지 검증
        int count = reviewMapper.countReservationOwnedByMember(request.getReservationId(), memberId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        Review review = Review.builder()
                .reservationId(request.getReservationId())
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        // 이미 리뷰가 작성된 예약인지 검증
        try {
            reviewMapper.insertReview(review);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ReviewErrorCode.REVIEW_DUPLICATE);
        }

        // 장소 테이블의 평균 별점 갱신
        Long placeId = reviewMapper.findPlaceIdByReservationId(request.getReservationId());
        placeMapper.updateAvgRating(placeId);

        return review.getId();
    }

    // 내가 작성한 리뷰 목록 조회
    public List<ReviewResponse> getMyReviews(Long memberId) {
        return reviewMapper.findReviewsByMemberId(memberId);
    }

    // 특정 장소의 리뷰 목록 조회
    public List<ReviewResponse> getReviewsByPlace(Long placeId) {
        return reviewMapper.findReviewsByPlaceId(placeId);
    }

    // 리뷰 단건 조회 (수정 폼 화면 용)
    public ReviewResponse getReview(Long memberId, Long reviewId) {
        ReviewResponse response = reviewMapper.findResponseById(reviewId)
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        int count = reviewMapper.countReviewOwnedByMember(reviewId, memberId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        return response;
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        // 리뷰 존재 여부 확인
        if (reviewMapper.countReviewById(reviewId) == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        // 본인이 작성한 리뷰인지 검증
        int count = reviewMapper.countReviewOwnedByMember(memberId, reviewId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        reviewMapper.updateReview(reviewId, request.getRating(), request.getContent());

        Long placeId = reviewMapper.findPlaceIdByReviewId(reviewId);
        placeMapper.updateAvgRating(placeId);
    }

    // 리뷰 삭제 (soft delete)
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        if (reviewMapper.countReviewById(reviewId) == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        int count = reviewMapper.countReviewOwnedByMember(memberId, reviewId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        reviewMapper.deleteReview(reviewId, LocalDateTime.now());

        Long placeId = reviewMapper.findPlaceIdByReviewId(reviewId);
        placeMapper.updateAvgRating(placeId);
    }
}
