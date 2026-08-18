package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
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

        // 리뷰가 속한 장소 조회 후 해당 place 잠금
        Long placeId = reviewMapper.findPlaceIdByReservationId(request.getReservationId());
        placeMapper.lockPlaceForUpdate(placeId);

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

        // 평균 별점 계산
        Double avgRating = reviewMapper.selectAvgRatingByPlaceId(placeId);

        // 장소 테이블의 평균 별점 갱신
        placeMapper.updateAvgRating(placeId, avgRating);

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
}
