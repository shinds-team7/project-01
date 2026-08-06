package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Review;
import com.example.petnow.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    // 리뷰 작성
    @Transactional
    public Long createReview(Long memberId, ReviewCreateRequest request) {
        // 이 예약이 실제로 로그인한 회원 본인의 예약인지 검증
        int count = reviewMapper.countReservationOwnedByMember(request.getReservationId(), memberId);
        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 예약에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        Review review = Review.builder()
                .reservationId(request.getReservationId())
                .content(request.getContent())
                .rating(request.getRating())
                .createdAt(LocalDateTime.now())
                .build();

        reviewMapper.insertReview(review);

        return review.getId();
    }
}