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
    private String sido;
    private String sigungu;
    private String eupmyeondong;
    private String roadAddress;
    private String address;
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
    private BigDecimal averageRating;
    private Integer reviewCount;
    private boolean supportsHourly;
    private boolean supportsPackage;
    private LocalTime packageCheckInTime;
    private LocalTime packageCheckOutTime;
    private PlaceStatus status;
    private boolean visible;
    private boolean bookmarked;
}
