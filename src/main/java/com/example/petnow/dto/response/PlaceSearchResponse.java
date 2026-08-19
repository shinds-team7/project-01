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

    /** 조건을 하나라도 걸었는지. 빈 결과 문구를 고를 때 쓴다. */
    public boolean isFiltered() {
        return regionLabel != null || dateLabel != null || timeLabel != null || petLabel != null;
    }
}
