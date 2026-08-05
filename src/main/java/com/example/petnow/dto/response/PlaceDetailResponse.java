package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.PlaceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private PlaceStatus status;
    private boolean visible;
}
