package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 예약 재고의 최소 단위. 3시간 격자 슬롯 하나에 대응한다.
 * 시 예약과 패키지 예약이 같은 격자를 공유하므로 두 유형의 교차 차단이 자동으로 성립한다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceAvailability {
    private Long id;
    private Long placeId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private SlotStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
