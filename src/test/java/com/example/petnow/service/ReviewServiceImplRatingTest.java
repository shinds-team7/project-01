package com.example.petnow.service;

import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ReviewErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * 리뷰 등록 시 장소의 평균 별점 갱신 (#213).
 *
 * <p>places.average_rating 은 조회할 때마다 집계하지 않으려고 비정규화해 둔 값이라
 * 리뷰가 들어올 때 같이 갱신되지 않으면 목록·상세에 옛 값이 계속 나간다.
 */
class ReviewServiceImplRatingTest {

    private ReviewMapper reviewMapper;
    private PlaceMapper placeMapper;
    private ReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        reviewMapper = mock(ReviewMapper.class);
        placeMapper = mock(PlaceMapper.class);
        service = new ReviewServiceImpl(reviewMapper, placeMapper);
    }

    @Test
    @DisplayName("리뷰를 등록하면 그 장소의 평균 별점이 다시 계산돼 저장된다")
    void updatesPlaceAverageRatingAfterInsert() {
        given(reviewMapper.countReservationOwnedByMember(3L, 1L)).willReturn(1);
        given(reviewMapper.findPlaceIdByReservationId(3L)).willReturn(9L);
        given(reviewMapper.selectAvgRatingByPlaceId(9L)).willReturn(4.5);

        service.createReview(1L, request());

        then(placeMapper).should().updateAvgRating(9L, 4.5);
    }

    @Test
    @DisplayName("남의 예약에 리뷰를 쓰려 하면 평균 별점을 건드리지 않는다")
    void doesNotTouchRatingWhenReservationIsNotMine() {
        given(reviewMapper.countReservationOwnedByMember(3L, 1L)).willReturn(0);

        assertThatThrownBy(() -> service.createReview(1L, request()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_FORBIDDEN);

        then(placeMapper).should(never()).updateAvgRating(anyLong(), anyDouble());
    }

    @Test
    @DisplayName("이미 리뷰가 있는 예약이면 평균 별점을 건드리지 않는다")
    void doesNotTouchRatingWhenReviewIsDuplicate() {
        // INSERT 가 실패했는데 평균만 다시 쓰면 리뷰 수와 별점이 어긋난다.
        given(reviewMapper.countReservationOwnedByMember(3L, 1L)).willReturn(1);
        willThrow(new DuplicateKeyException("uk_reviews_reservation"))
                .given(reviewMapper).insertReview(any());

        assertThatThrownBy(() -> service.createReview(1L, request()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_DUPLICATE);

        then(placeMapper).should(never()).updateAvgRating(anyLong(), anyDouble());
    }

    private ReviewCreateRequest request() {
        return ReviewCreateRequest.builder()
                .reservationId(3L)
                .rating(5)
                .content("마당이 넓어서 좋았어요")
                .build();
    }
}
