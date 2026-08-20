package com.example.petnow.dto.request;

import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PlaceAvailabilityServiceImpl;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 홈의 검색 조건 카드가 {@code GET /nearby} 로 보내는 필터 값. (#7)
 *
 * <p>화면에서 온 날것의 값만 담는다. 일정 모드 분기·마릿수·크기 조건 같은 판단은
 * {@link com.example.petnow.dto.request.PlaceFilterCriteria} 로 정규화한 뒤에 한다.
 *
 * <p><b>종료 시각 24:00 규약.</b> 하루의 마지막 슬롯은 21:00~24:00 인데 {@code LocalTime} 에는
 * 24:00 이 없다. 그래서 종료 시각 select 는 화면에 "24:00" 을 보여주고 값은 {@code 00:00} 을 보낸다.
 * 즉 <b>종료 시각이 자정이면 그날의 끝</b>을 뜻한다. 시작 시각에는 이 규약을 쓰지 않는다
 * (시작 00:00 은 그대로 그날의 시작이다).
 *
 * <p>검증은 전부 {@code @AssertTrue} 로 두어 실패해도 예외가 아니라 {@code BindingResult} 의
 * 폼 에러가 되게 한다. 잘못된 조건 하나로 500 을 내면 안 된다.
 */
@Getter
@Setter
public class PlaceFilterRequest {

    /** 구간 길이 상한. 이보다 긴 조회는 슬롯 스캔이 커지기만 하고 쓸모가 없다. */
    private static final int MAX_RANGE_DAYS = 30;

    /** 지역구(sigungu) 복수 선택. 비어 있으면 지역 조건을 걸지 않는다. */
    private List<String> regions;

    /** 장소명·소개글·호스트 닉네임에서 찾을 검색어. 빈 문자열은 서비스에서 조건 없이 정규화한다. */
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 비우면 시작일과 같은 날, 즉 하루 예약으로 본다. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /** 맡길 반려견. 서버에서 distinct 하고 본인 소유인지 확인한다. */
    private List<Long> petIds;

    private PlaceType placeType;

    /** 정렬 키. 값 검증은 매퍼의 allowlist 가 한다. */
    private String sort;

    /** 매퍼 {@code findByFilter} 의 정렬 allowlist. 여기에 없는 값은 최신순으로 떨어진다. */
    private static final String SORT_PRICE = "price";
    private static final String SORT_RATING = "rating";
    public static final String SORT_LATEST = "latest";

    /**
     * 화면이 어느 정렬 칩을 켤지 고르는 값.
     *
     * <p>매퍼는 allowlist 밖의 값을 조용히 최신순으로 떨어뜨린다. 화면이 {@code sort} 원본을 그대로
     * 비교하면 {@code ?sort=xxx} 같은 입력에서 아무 칩도 안 켜져 "정렬이 안 걸린 것"처럼 보인다.
     * 매퍼와 같은 판정을 여기서 한 번 하고 화면은 그 결과만 쓴다.
     */
    public String normalizedSort() {
        return SORT_PRICE.equals(sort) || SORT_RATING.equals(sort) ? sort : SORT_LATEST;
    }

    /** 반려견을 한 마리라도 골랐는지. 비로그인 판단에 쓴다. */
    public boolean hasPetSelection() {
        return petIds != null && petIds.stream().anyMatch(id -> id != null);
    }

    /** 날짜를 아예 고르지 않았으면 일정 조건 자체를 걸지 않는다. */
    public boolean hasSchedule() {
        return startDate != null;
    }

    /** 종료일 미입력은 시작일과 같은 날로 취급한다. */
    public LocalDate resolvedEndDate() {
        return endDate == null ? startDate : endDate;
    }

    /** 연속 여러 날, 즉 패키지 예약인지. */
    public boolean isMultiDay() {
        return hasSchedule() && resolvedEndDate().isAfter(startDate);
    }

    @AssertTrue(message = "종료일은 시작일보다 빠를 수 없어요")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "예약 기간은 최대 30일까지 선택할 수 있어요")
    public boolean isDateRangeWithinLimit() {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return true;
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1 <= MAX_RANGE_DAYS;
    }

    @AssertTrue(message = "지난 날짜는 선택할 수 없어요")
    public boolean isStartDateNotPast() {
        return startDate == null || !startDate.isBefore(LocalDate.now());
    }

    @AssertTrue(message = "시간을 선택하려면 날짜를 먼저 골라 주세요")
    public boolean isTimeAccompaniedByDate() {
        return hasSchedule() || (startTime == null && endTime == null);
    }

    /** "복수 날짜 선택 시 시간 선택 비활성화" 완료 조건의 서버측 보장. 화면 JS 만 믿지 않는다. */
    @AssertTrue(message = "여러 날을 선택하면 시간은 고를 수 없어요")
    public boolean isTimeAllowedForRange() {
        return !isMultiDay() || (startTime == null && endTime == null);
    }

    @AssertTrue(message = "시작 시간과 종료 시간을 함께 선택해 주세요")
    public boolean isTimePairComplete() {
        return (startTime == null) == (endTime == null);
    }

    @AssertTrue(message = "종료 시간은 시작 시간보다 늦어야 해요")
    public boolean isTimeOrderValid() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endMinuteOfDay() > startMinuteOfDay();
    }

    @AssertTrue(message = "시간은 3시간 단위(00:00, 03:00 …)로 선택해 주세요")
    public boolean isTimeOnSlotGrid() {
        return isOnGrid(startTime) && isOnGrid(endTime);
    }

    /** 시작 시각의 자정 기준 분. */
    public int startMinuteOfDay() {
        return startTime.getHour() * 60 + startTime.getMinute();
    }

    /** 종료 시각의 자정 기준 분. 자정은 다음 날 0시(=24:00)로 읽는다. */
    public int endMinuteOfDay() {
        return endTime.equals(LocalTime.MIDNIGHT) ? 24 * 60 : endTime.getHour() * 60 + endTime.getMinute();
    }

    private static boolean isOnGrid(LocalTime time) {
        return time == null
                || (time.getMinute() == 0
                && time.getSecond() == 0
                && time.getHour() % PlaceAvailabilityServiceImpl.SLOT_HOURS == 0);
    }
}
