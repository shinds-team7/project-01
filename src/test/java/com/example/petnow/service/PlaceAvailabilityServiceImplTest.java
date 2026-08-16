package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceOperatingPolicyUpdateRequest;
import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceAvailability;
import com.example.petnow.entity.SlotStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.PlaceAvailabilityMapper;
import com.example.petnow.mapper.PlaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlaceAvailabilityServiceImplTest {

    private PlaceAvailabilityMapper availabilityMapper;
    private PlaceMapper placeMapper;
    private PlaceAvailabilityServiceImpl service;

    @BeforeEach
    void setUp() {
        availabilityMapper = mock(PlaceAvailabilityMapper.class);
        placeMapper = mock(PlaceMapper.class);
        service = new PlaceAvailabilityServiceImpl(availabilityMapper, placeMapper);
    }

    @Test
    void createsEightThreeHourSlotsPerDay() {
        given(placeMapper.findById(1L)).willReturn(ownedPlace());
        List<PlaceAvailability> inserted = new ArrayList<>();
        given(availabilityMapper.insertSlots(anyList())).willAnswer(invocation -> {
            List<PlaceAvailability> slots = invocation.getArgument(0);
            inserted.addAll(slots);
            return slots.size();
        });

        int count = service.createSlots(
                7L, 1L, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 20));

        assertThat(count).isEqualTo(8);
        assertThat(inserted).hasSize(8);
        assertThat(inserted.get(0).getStartAt().getHour()).isZero();
        assertThat(inserted.get(7).getStartAt().getHour()).isEqualTo(21);
        assertThat(inserted).allMatch(slot -> slot.getStatus() == SlotStatus.OPEN);
    }

    @Test
    void rejectsSlotCreationByAnotherHost() {
        given(placeMapper.findById(1L)).willReturn(ownedPlace());

        assertThatThrownBy(() -> service.createSlots(
                99L, 1L, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void packageDayIsSelectableOnlyWhenAllSevenSlotsAreOpen() {
        Place place = ownedPlace();
        given(placeMapper.findById(1L)).willReturn(place);
        given(availabilityMapper.countOpenSlotsInRange(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .willReturn(7);

        List<PackageDayResponse> days = service.getPackageDays(
                1L, java.time.YearMonth.of(2026, 8));

        assertThat(days).hasSize(31).allMatch(PackageDayResponse::isSelectable);
    }

    @Test
    void savesPackagePolicyWithGridAlignedDefaultTimes() {
        given(placeMapper.findById(1L)).willReturn(ownedPlace());
        given(placeMapper.updateOperatingPolicy(
                1L, true, true, LocalTime.of(15, 0), LocalTime.of(12, 0)))
                .willReturn(1);
        PlaceOperatingPolicyUpdateRequest request = new PlaceOperatingPolicyUpdateRequest();
        request.setSupportsHourly(true);
        request.setSupportsPackage(true);

        service.updateOperatingPolicy(7L, 1L, request);

        verify(placeMapper).updateOperatingPolicy(
                1L, true, true, LocalTime.of(15, 0), LocalTime.of(12, 0));
    }

    @Test
    void returnsPersistedSlotPeriodForHostScreen() {
        given(placeMapper.findById(1L)).willReturn(ownedPlace());
        PlaceSlotPeriodResponse period = new PlaceSlotPeriodResponse(
                LocalDate.of(2026, 8, 18), LocalDate.of(2026, 8, 24));
        given(availabilityMapper.findSlotPeriodByPlaceId(1L)).willReturn(period);

        PlaceSlotPeriodResponse result = service.getSlotPeriod(7L, 1L);

        assertThat(result.getFromDate()).isEqualTo(LocalDate.of(2026, 8, 18));
        assertThat(result.getToDate()).isEqualTo(LocalDate.of(2026, 8, 24));
    }

    private Place ownedPlace() {
        return Place.builder()
                .id(1L)
                .hostUserId(7L)
                .supportsHourly(true)
                .supportsPackage(true)
                .build();
    }
}
