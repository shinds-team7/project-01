package com.example.petnow.dto.response;

import lombok.Getter;

import java.time.LocalDate;


/**
 * 리뷰 조회 응답 DTO
 * (내 리뷰 조회 / 장소별 리뷰 조회에 공용으로 사용)
 */
@Getter
public class ReviewResponse {

    private Long id;
    private Long reservationId;
    private Long memberId;              // reservations.guest_user_id
    private Long placeId;               // reservations.place_id
    private String placeName;           // places.name
    private LocalDate checkInAt;    // reservations.check_in_at
    private String content;
    private int rating;
}
