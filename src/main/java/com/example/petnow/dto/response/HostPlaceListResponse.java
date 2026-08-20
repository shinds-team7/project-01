package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostPlaceListResponse {

    private Long id;
    private String name;
//  private String thumbnailUrl; TODO:추후 게시글 사진 이미지 첨부 기능 구현 후 DTO 추가
//  private int reservationCount;
    private PlaceStatus status;

    /**
     * 평균 별점. 다른 장소 DTO 와 같은 {@code BigDecimal} 로 맞춘다.
     * {@code places.average_rating} 이 {@code DECIMAL(3,2)} 라 {@code Double} 로 받으면
     * 화면에서 0.1 단위 표기가 흔들린다.
     */
    private BigDecimal averageRating;

    /** 별점을 그릴지. 판단 근거는 {@link PlaceListResponse#hasRating()} 과 같다. */
    public boolean hasRating() {
        return averageRating != null && averageRating.compareTo(BigDecimal.ZERO) > 0;
    }

}

