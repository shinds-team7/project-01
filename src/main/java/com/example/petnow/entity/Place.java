package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Place {
    private Long id;
    private Long userId;
    private String title;
    private String address;
    private String detailAddress;
    private String info;
    private PlaceType placeType;
    private int placeArea;
    private int maxDogCount;
    private boolean optionSmallDog;
    private boolean optionMediumDog;
    private boolean optionLargeDog;
    private boolean optionHomeCam;
    private boolean optionRealTimePhoto;
    private boolean optionYard;
    private boolean optionWalkService;
    private boolean optionEtc;
    private String optionText;
    private Long hourPrice;
    private Long dayPrice;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
