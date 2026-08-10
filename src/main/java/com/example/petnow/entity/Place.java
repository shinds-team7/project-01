package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Place {
    private Long id;
    private Long hostUserId;
    private String hostUserNickname;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
