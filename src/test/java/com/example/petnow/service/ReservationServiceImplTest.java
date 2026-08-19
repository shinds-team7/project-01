package com.example.petnow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.ReservationType;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ReservationErrorCode;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceAvailabilityMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private PlaceMapper placeMapper;

    @Mock
    private PetMapper petMapper;

    @Mock
    private PlaceAvailabilityMapper placeAvailabilityMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void rejectsPastReservationBeforeCheckingSlots() {
        given(placeMapper.findById(1L)).willReturn(Place.builder()
                .id(1L)
                .supportsHourly(true)
                .build());

        LocalDateTime checkIn = LocalDateTime.now().minusHours(6);
        ReservationRequest request = ReservationRequest.builder()
                .placeId(1L)
                .petIds(List.of(1L))
                .reservationType(ReservationType.SAME_DAY)
                .checkIn(checkIn)
                .checkOut(checkIn.plusHours(3))
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.saveReservation(request, 1L));

        assertEquals(ReservationErrorCode.RESERVATION_IN_PAST, exception.getErrorCode());
    }

    @Test
    void resolvesHourlyRangeIncludingBothBoundarySlots() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime dayStart = date.atStartOfDay();
        List<PlaceSlotResponse> slots = List.of(
                slot(1L, dayStart.plusHours(9)),
                slot(2L, dayStart.plusHours(12)),
                slot(3L, dayStart.plusHours(15)));

        given(placeMapper.findById(1L)).willReturn(Place.builder()
                .id(1L)
                .supportsHourly(true)
                .hourlyPrice(BigDecimal.valueOf(10_000))
                .build());
        given(placeAvailabilityMapper.findSlotsByPlaceAndPeriod(
                1L, dayStart, dayStart.plusDays(1))).willReturn(slots);

        ReservationStepResponse response = reservationService.resolveHourly(
                1L, date.toString(), 1L, 3L);

        assertEquals("confirm", response.getStep());
        assertEquals(dayStart.plusHours(9), response.getCheckIn());
        assertEquals(dayStart.plusHours(18), response.getCheckOut());
        assertEquals(BigDecimal.valueOf(90_000), response.getTotalPrice());
    }

    @Test
    void resolvesConfirmForHourlyOnlyPlace() {
        LocalDateTime checkIn = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime checkOut = checkIn.plusHours(3);
        given(placeMapper.findById(1L)).willReturn(Place.builder()
                .id(1L)
                .supportsHourly(true)
                .supportsPackage(false)
                .hourlyPrice(BigDecimal.valueOf(10_000))
                .build());

        ReservationStepResponse response = reservationService.resolveConfirm(
                1L, ReservationType.SAME_DAY, checkIn, checkOut);

        assertEquals("confirm", response.getStep());
        assertEquals(ReservationType.SAME_DAY, response.getReservationType());
        assertEquals(checkIn, response.getCheckIn());
        assertEquals(checkOut, response.getCheckOut());
        assertEquals(BigDecimal.valueOf(30_000), response.getTotalPrice());
    }

    private PlaceSlotResponse slot(Long id, LocalDateTime startAt) {
        return PlaceSlotResponse.builder()
                .slotId(id)
                .startAt(startAt)
                .endAt(startAt.plusHours(3))
                .status("OPEN")
                .build();
    }
}
