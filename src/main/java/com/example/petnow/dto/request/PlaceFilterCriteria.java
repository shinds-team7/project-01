package com.example.petnow.dto.request;

import com.example.petnow.entity.PlaceType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 매퍼가 그대로 읽는 정규화된 조회 조건. (#7)
 *
 * <p>{@link PlaceFilterRequest} 의 날것 값(빈 문자열, 안 고른 종료일, 중복 pet id)은
 * 서비스에서 여기로 옮겨 담으면서 정리된다. 매퍼가 요청 DTO 를 직접 보지 않게 해서
 * "화면이 뭘 보냈는지"와 "쿼리가 무엇을 거는지"를 분리한다.
 *
 * <p>일정 조건은 아래 네 모드 중 하나다. XML 의 {@code <if>} 가 boolean getter 로 분기한다.
 * <ul>
 *   <li>{@code NONE} — 날짜를 안 골랐다. 슬롯 조건 없음</li>
 *   <li>{@code HOURLY} — 하루 + 시간. {@code supports_hourly} + 고정 구간의 OPEN 슬롯 수</li>
 *   <li>{@code DAY} — 하루 + 시간 미선택. 그날 OPEN 슬롯이 하나라도 있으면 통과</li>
 *   <li>{@code PACKAGE} — 연속 여러 날. {@code supports_package} + 장소별 입·퇴실 시각 구간</li>
 * </ul>
 */
@Getter
@Builder
public class PlaceFilterCriteria {

    public enum ScheduleMode {
        NONE, HOURLY, DAY, PACKAGE
    }

    private final List<String> regions;
    private final String keyword;
    private final PlaceType placeType;

    /** 고른 반려견 수. 0 이면 마릿수·크기 조건을 걸지 않는다. */
    private final int petCount;

    private final boolean requiresSmallDog;
    private final boolean requiresMediumDog;
    private final boolean requiresLargeDog;

    private final ScheduleMode scheduleMode;

    /** HOURLY·DAY 가 쓰는 조회 구간. */
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    /**
     * PACKAGE 가 쓰는 날짜. 숙박 구간의 시·종 시각은 장소마다 다르므로
     * (p.package_check_in_time / p.package_check_out_time) 여기서는 날짜만 넘기고
     * 실제 구간은 쿼리 안에서 장소별로 만든다.
     */
    private final LocalDate startDate;
    private final LocalDate endDate;

    /** HOURLY 에서 구간을 채우는 데 필요한 3시간 슬롯 개수. 모든 장소가 같아서 미리 계산해 넘긴다. */
    private final int requiredSlots;

    /** 정렬 키. 매퍼가 allowlist 로 해석하므로 여기 값이 SQL 에 그대로 박히지 않는다. */
    private final String sort;

    public boolean isHourlyMode() {
        return scheduleMode == ScheduleMode.HOURLY;
    }

    public boolean isDayMode() {
        return scheduleMode == ScheduleMode.DAY;
    }

    public boolean isPackageMode() {
        return scheduleMode == ScheduleMode.PACKAGE;
    }
}
