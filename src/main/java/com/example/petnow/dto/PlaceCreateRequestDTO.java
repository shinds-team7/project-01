package com.example.petnow.dto;

import com.example.petnow.entity.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceCreateRequestDTO {

    // 사진리스트인데 타입은?사진
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
}
