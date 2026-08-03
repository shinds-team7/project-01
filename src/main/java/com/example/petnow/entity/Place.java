package com.example.petnow.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
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
    private Integer placeArea;
    private Integer maxDogCount;
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
    private PlaceStatus placeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
