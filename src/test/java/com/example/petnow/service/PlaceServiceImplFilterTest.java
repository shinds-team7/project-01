package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceFilterCriteria;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 조건 필터링의 정규화 규칙을 고정한다. (#7)
 *
 * <p>여기서 확인하는 건 "화면에서 온 값이 어떤 조회 조건이 되는가" 하나다.
 * 슬롯 개수가 맞는지 같은 SQL 쪽 판정은 {@code PlaceMapperFilterStatementTest} 가 본다.
 */
class PlaceServiceImplFilterTest {

    private static final LocalDate DAY = LocalDate.now().plusDays(3);

    private PlaceMapper placeMapper;
    private PetMapper petMapper;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        petMapper = mock(PetMapper.class);
        placeService = new PlaceServiceImpl(placeMapper, mock(AuthMapper.class), petMapper);
        given(placeMapper.findByFilter(any())).willReturn(List.of());
    }

    @Test
    @DisplayName("날짜를 안 고르면 슬롯 조건을 걸지 않는다")
    void noScheduleMeansNoSlotCondition() {
        PlaceFilterCriteria criteria = capture(request());

        assertThat(criteria.getScheduleMode()).isEqualTo(PlaceFilterCriteria.ScheduleMode.NONE);
        assertThat(criteria.isHourlyMode()).isFalse();
        assertThat(criteria.isDayMode()).isFalse();
        assertThat(criteria.isPackageMode()).isFalse();
        assertThat(criteria.getStartAt()).isNull();
    }

    @Test
    @DisplayName("하루 + 시간을 고르면 시 예약이 되고 필요한 슬롯 수를 미리 계산한다")
    void oneDayWithTimeBecomesHourly() {
        PlaceFilterRequest request = request();
        request.setStartDate(DAY);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.isHourlyMode()).isTrue();
        assertThat(criteria.getStartAt()).isEqualTo(DAY.atTime(9, 0));
        assertThat(criteria.getEndAt()).isEqualTo(DAY.atTime(18, 0));
        // 9시간 = 3시간짜리 슬롯 3칸
        assertThat(criteria.getRequiredSlots()).isEqualTo(3);
    }

    @Test
    @DisplayName("종료 시각 24:00 은 자정이 아니라 그날의 끝이다")
    void midnightEndMeansEndOfDay() {
        PlaceFilterRequest request = request();
        request.setStartDate(DAY);
        request.setStartTime(LocalTime.of(21, 0));
        request.setEndTime(LocalTime.MIDNIGHT);

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.isHourlyMode()).isTrue();
        assertThat(criteria.getEndAt()).isEqualTo(DAY.plusDays(1).atStartOfDay());
        assertThat(criteria.getRequiredSlots()).isEqualTo(1);
    }

    @Test
    @DisplayName("하루만 고르고 시간을 비우면 그날 열린 슬롯을 찾는 탐색이 된다")
    void oneDayWithoutTimeBecomesDaySearch() {
        PlaceFilterRequest request = request();
        request.setStartDate(DAY);
        request.setEndDate(DAY);

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.isDayMode()).isTrue();
        assertThat(criteria.getStartAt()).isEqualTo(DAY.atStartOfDay());
        assertThat(criteria.getEndAt()).isEqualTo(DAY.plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("연속 여러 날은 패키지가 되고 시각은 장소별로 정하도록 날짜만 넘긴다")
    void multipleDaysBecomePackage() {
        PlaceFilterRequest request = request();
        request.setStartDate(DAY);
        request.setEndDate(DAY.plusDays(2));

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.isPackageMode()).isTrue();
        assertThat(criteria.getStartDate()).isEqualTo(DAY);
        assertThat(criteria.getEndDate()).isEqualTo(DAY.plusDays(2));
        // 입·퇴실 시각이 장소마다 달라 자바에서 슬롯 수를 정할 수 없다
        assertThat(criteria.getRequiredSlots()).isZero();
        assertThat(criteria.getStartAt()).isNull();
    }

    @Test
    @DisplayName("같은 아이를 여러 번 보내도 마릿수는 한 번만 센다")
    void duplicatedPetIdsCountOnce() {
        given(petMapper.getPetList(7L)).willReturn(List.of(pet(1L, "초코", Pet.Size.SMALL)));
        PlaceFilterRequest request = request();
        request.setPetIds(List.of(1L, 1L));

        PlaceFilterCriteria criteria = capture(7L, request);

        assertThat(criteria.getPetCount()).isEqualTo(1);
        assertThat(criteria.isRequiresSmallDog()).isTrue();
    }

    @Test
    @DisplayName("크기를 적지 않은 아이는 크기 조건을 만들지 않는다")
    void petWithoutSizeAddsNoSizeCondition() {
        given(petMapper.getPetList(7L)).willReturn(List.of(
                pet(1L, "초코", Pet.Size.SMALL),
                pet(2L, "보리", null)));
        PlaceFilterRequest request = request();
        request.setPetIds(List.of(1L, 2L));

        PlaceFilterCriteria criteria = capture(7L, request);

        // 마릿수는 두 마리 그대로지만 크기 조건은 소형견 하나뿐이다
        assertThat(criteria.getPetCount()).isEqualTo(2);
        assertThat(criteria.isRequiresSmallDog()).isTrue();
        assertThat(criteria.isRequiresMediumDog()).isFalse();
        assertThat(criteria.isRequiresLargeDog()).isFalse();
    }

    @Test
    @DisplayName("내 아이가 아닌 id 가 섞이면 조용히 무시하지 않고 예외를 던진다")
    void rejectsPetOfAnotherUser() {
        given(petMapper.getPetList(7L)).willReturn(List.of(pet(1L, "초코", Pet.Size.SMALL)));
        PlaceFilterRequest request = request();
        request.setPetIds(List.of(1L, 99L));

        assertThatThrownBy(() -> placeService.searchPlaces(7L, request))
                .isInstanceOf(BusinessException.class);

        verify(placeMapper, never()).findByFilter(any());
    }

    @Test
    @DisplayName("비로그인인데 아이 조건이 오면 조회하지 않는다")
    void rejectsPetSelectionWithoutLogin() {
        PlaceFilterRequest request = request();
        request.setPetIds(List.of(1L));

        assertThatThrownBy(() -> placeService.searchPlaces(null, request))
                .isInstanceOf(BusinessException.class);

        verify(placeMapper, never()).findByFilter(any());
    }

    @Test
    @DisplayName("빈 지역 값은 조건에서 걷어낸다")
    void blankRegionsAreDropped() {
        PlaceFilterRequest request = request();
        request.setRegions(List.of("성동구", "", "성동구", "  "));

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.getRegions()).containsExactly("성동구");
    }

    @Test
    @DisplayName("키워드는 앞뒤 공백을 걷어 매퍼에 넘긴다")
    void keywordIsTrimmed() {
        PlaceFilterRequest request = request();
        request.setKeyword("  마당 있는 집  ");

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.getKeyword()).isEqualTo("마당 있는 집");
    }

    @Test
    @DisplayName("공백뿐인 키워드는 조회 조건을 만들지 않는다")
    void blankKeywordAddsNoCondition() {
        PlaceFilterRequest request = request();
        request.setKeyword("   ");

        PlaceFilterCriteria criteria = capture(request);

        assertThat(criteria.getKeyword()).isNull();
    }

    @Test
    @DisplayName("고른 조건을 화면 문구로 요약한다")
    void buildsSummaryLabels() {
        given(petMapper.getPetList(7L)).willReturn(List.of(
                pet(1L, "초코", Pet.Size.SMALL),
                pet(2L, "보리", Pet.Size.LARGE)));
        PlaceFilterRequest request = request();
        request.setRegions(List.of("성동구", "광진구"));
        request.setStartDate(LocalDate.of(2026, 8, 20));
        request.setEndDate(LocalDate.of(2026, 8, 22));
        request.setPetIds(List.of(1L, 2L));

        PlaceSearchResponse result = placeService.searchPlaces(7L, request);

        assertThat(result.getRegionLabel()).isEqualTo("성동구 외 1곳");
        assertThat(result.getDateLabel()).isEqualTo("8월 20일 ~ 8월 22일");
        assertThat(result.getTimeLabel()).isNull();
        assertThat(result.getPetLabel()).isEqualTo("초코 외 1마리");
        assertThat(result.isFiltered()).isTrue();
    }

    @Test
    @DisplayName("조건이 없으면 요약 문구도 없고 결과 0건도 예외가 아니다")
    void emptyResultIsNotAnError() {
        PlaceSearchResponse result = placeService.searchPlaces(null, request());

        assertThat(result.getPlaces()).isEmpty();
        assertThat(result.isFiltered()).isFalse();
    }

    @Test
    @DisplayName("종료 24:00 은 문구에서도 00:00 이 아니라 24:00 으로 적는다")
    void midnightEndLabel() {
        PlaceFilterRequest request = request();
        request.setStartDate(DAY);
        request.setStartTime(LocalTime.of(21, 0));
        request.setEndTime(LocalTime.MIDNIGHT);

        assertThat(placeService.searchPlaces(null, request).getTimeLabel()).isEqualTo("21:00 ~ 24:00");
    }

    private PlaceFilterCriteria capture(PlaceFilterRequest request) {
        return capture(null, request);
    }

    private PlaceFilterCriteria capture(Long loginUserId, PlaceFilterRequest request) {
        placeService.searchPlaces(loginUserId, request);

        ArgumentCaptor<PlaceFilterCriteria> captor = ArgumentCaptor.forClass(PlaceFilterCriteria.class);
        verify(placeMapper).findByFilter(captor.capture());
        return captor.getValue();
    }

    private static PlaceFilterRequest request() {
        return new PlaceFilterRequest();
    }

    private static PetListResponse pet(long id, String name, Pet.Size size) {
        return PetListResponse.builder().id(id).name(name).size(size).build();
    }
}
