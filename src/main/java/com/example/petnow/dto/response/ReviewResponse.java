package com.example.petnow.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;


/**
 * 리뷰 조회 응답 DTO
 * (내 리뷰 조회 / 장소별 리뷰 조회에 공용으로 사용)
 */
@Getter
public class ReviewResponse {

    private Long id;
    private Long reservationId;
    private Long memberId;          // reservation.guest_user_id
    private Long placeId;           // reservation.place_id
    private String placeName;       // places.name
    private String stayInfo = "";   // 우선 빈문자열 반환
    private String content;
    private int rating;
    private LocalDateTime createdAt;
}
