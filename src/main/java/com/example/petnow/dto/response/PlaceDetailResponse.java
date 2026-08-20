package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.PlaceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class PlaceDetailResponse {

    private Long id;
    private Long hostUserId;
    private String nickname;
    private String name;
    private String description;
    private PlaceType placeType;
    private BigDecimal areaSize;
    private Integer capacity;
    private boolean allowsSmallDog;
    private boolean allowsMediumDog;
    private boolean allowsLargeDog;
    private boolean providesHomeCamera;
    private boolean providesRealtimePhoto;
    private boolean providesYard;
    private boolean providesWalk;
    private String otherOptions;
    private BigDecimal hourlyPrice;
    private BigDecimal nightlyPrice;
    private boolean supportsHourly;
    private boolean supportsPackage;
    private LocalTime packageCheckInTime;
    private LocalTime packageCheckOutTime;
    private PlaceStatus status;
    private boolean visible;
    private boolean bookmarked;

    /**
     * 평균 별점. {@code places.average_rating} 을 그대로 읽는다.
     * 리뷰가 없으면 {@code 0.00} 이고, 그 경우 화면은 별점 대신 리뷰 유도 문구를 그린다.
     */
    private BigDecimal averageRating;

    /** 별점을 그릴지. 판단 근거는 {@link PlaceListResponse#hasRating()} 과 같다. */
    public boolean hasRating() {
        return averageRating != null && averageRating.compareTo(BigDecimal.ZERO) > 0;
    }
}
