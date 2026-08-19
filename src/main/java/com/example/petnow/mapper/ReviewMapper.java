package com.example.petnow.mapper;

import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ReviewMapper {

    // 리뷰 등록
    void insertReview(Review review);

    // 리뷰 작성 권한 체크 (해당 예약이 로그인된 유저의 예약이 맞는지 학인)
    int countReservationOwnedByMember(@Param("reservationId") Long reservationId,
                                      @Param("memberId") Long memberId);

    // 내가 작성한 리뷰 목록 조회 (reservation과 조인)
    List<ReviewResponse> findReviewsByMemberId(@Param("memberId") Long memberId);

    // 특정 장소의 리뷰 목록 조회 (reservation과 조인)
    List<ReviewResponse> findReviewsByPlaceId(@Param("placeId") Long placeId);

    // 특정 예약의 placeId 조회
    Long findPlaceIdByReservationId(@Param("reservationId") Long reservationId);

    // 리뷰 단건 조회 (화면 표시용)
    Optional<ReviewResponse> findResponseById(@Param("reviewId") Long reviewId);

    int countReviewById(@Param("reviewId") Long reviewId);
    int countReviewOwnedByMember(@Param("memberId") Long memberId, @Param("reviewId") Long reviewId);
    Long findPlaceIdByReviewId(@Param("reviewId") Long reviewId);

    // 리뷰 수정
    void updateReview(@Param("reviewId") Long reviewId, @Param("rating") Integer rating, @Param("content") String content);

    // 리뷰 삭제
    void deleteReview(@Param("reviewId") Long reviewId, @Param("deletedAt") LocalDateTime deletedAt);
}
