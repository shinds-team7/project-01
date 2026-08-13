package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 패키지 예약 달력의 날짜 한 칸.
 * 그 날짜의 숙박 구간 슬롯이 전부 OPEN 일 때만 선택할 수 있다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageDayResponse {
    private LocalDate date;
    private boolean selectable;
}
