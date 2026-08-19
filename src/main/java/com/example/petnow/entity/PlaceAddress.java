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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceAddress {
    private Long id;
    private Long placeId;
    private String addressCode;
    private String sido;
    private String sigungu;
    private String eupmyeondong;
    private String roadAddress;
    private String detailAddress;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
