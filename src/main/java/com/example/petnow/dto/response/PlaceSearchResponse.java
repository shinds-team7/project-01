package com.example.petnow.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 조건 필터링 결과. (#7)
 *
 * <p>결과 0건은 예외가 아니라 빈 리스트다. 화면은 그때 안내 문구를 그린다.
 * 라벨 넷은 화면 상단의 "적용된 조건" 칩과 홈의 요약 문구가 함께 쓴다.
 * 조건을 안 걸었으면 {@code null} 이라 칩이 그려지지 않는다.
 */
@Getter
@Builder
public class PlaceSearchResponse {

    private final List<PlaceListResponse> places;
    private final String regionLabel;
    private final String dateLabel;
    private final String timeLabel;
    private final String petLabel;

    /** 고른 장소 유형. 안 골랐으면 {@code null} 이라 칩이 그려지지 않는다. */
    private final String typeLabel;

    /**
     * 조건을 하나라도 걸었는지. 빈 결과 문구를 고를 때 쓴다.
     *
     * <p>여기 빠진 조건은 결과가 0건일 때 "조건에 맞는 공간이 없어요" 대신
     * "주변에 공개된 공간이 없어요" 로 안내돼, 조건을 걸었는데도 안 건 것처럼 읽힌다.
     * 화면에 새 조건을 붙일 때 라벨과 이 판정을 같이 늘려야 하는 이유다.
     */
    public boolean isFiltered() {
        return regionLabel != null || dateLabel != null || timeLabel != null
                || petLabel != null || typeLabel != null;
    }
}
