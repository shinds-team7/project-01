package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostPlaceListResponse {

    private Long id;
    private String name;
//  private String thumbnailUrl; TODO:추후 게시글 사진 이미지 첨부 기능 구현 후 DTO 추가
//  private Double averageRating;
//  private int reservationCount;
    private PlaceStatus status;

}

