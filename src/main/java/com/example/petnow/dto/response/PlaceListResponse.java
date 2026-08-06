package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceListResponse {

    private Long id;
    private String nickname;
    private String name;
    private String description;
    private PlaceType placeType;
    private boolean allowsSmallDog;
    private boolean allowsMediumDog;
    private boolean allowsLargeDog;
    private boolean providesHomeCamera;
    private boolean providesRealtimePhoto;
    private boolean providesYard;
    private boolean providesWalk;
    private BigDecimal hourlyPrice;
}
