package com.example.petnow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationType;
import com.example.petnow.exception.AuthErrorCode;
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

    @Test
    void packageCheckoutDateStaysSelectableWhenSlotsAfterCheckoutAreBlocked() {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = checkInDate.plusDays(1);
        List<PlaceSlotResponse> allSlots = packageBoundarySlots(checkInDate);
        List<PlaceSlotResponse> reservationSlots = allSlots.stream()
                .filter(slot -> !slot.getStartAt().isBefore(checkInDate.atTime(15, 0)))
                .filter(slot -> slot.getStartAt().isBefore(checkOutDate.atTime(12, 0)))
                .toList();

        given(placeMapper.findById(1L)).willReturn(Place.builder()
                .id(1L)
                .supportsPackage(true)
                .nightlyPrice(BigDecimal.valueOf(50_000))
                .build());
        given(placeAvailabilityMapper.findSlotPeriodByPlaceId(1L))
                .willReturn(new PlaceSlotPeriodResponse(checkInDate, checkOutDate));
        given(placeAvailabilityMapper.findSlotsByPlaceAndPeriod(
                1L, checkInDate.atStartOfDay(), checkOutDate.plusDays(1).atStartOfDay()))
                .willReturn(allSlots);
        given(placeAvailabilityMapper.findSlotsByPlaceAndPeriod(
                1L, checkInDate.atTime(15, 0), checkOutDate.atTime(12, 0)))
                .willReturn(reservationSlots);

        ReservationStepResponse selectStep = reservationService.resolvePackage(1L, null, null);
        ReservationStepResponse confirmStep = reservationService.resolvePackage(
                1L,
                checkInDate.toString(),
                checkOutDate.toString());

        assertEquals(true, selectStep.getDays().get(0).isSelectable());
        assertEquals(true, selectStep.getDays().get(1).isSelectable());
        assertEquals("confirm", confirmStep.getStep());
        assertEquals(checkOutDate.atTime(12, 0), confirmStep.getCheckOut());
    }

    @Test
    void cancelsConfirmedReservationBeforeUse() {
        given(reservationMapper.findById(1L)).willReturn(reservation(ReservationStatus.CONFIRMED,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4)));
        given(reservationMapper.cancelReservation(1L)).willReturn(1);

        reservationService.cancelReservation(1L, 10L);

        then(reservationMapper).should().cancelReservation(1L);
    }

    @Test
    void refusesToCancelConfirmedReservationInUse() {
        given(reservationMapper.findById(1L)).willReturn(reservation(ReservationStatus.CONFIRMED,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(1L, 10L));

        assertEquals(ReservationErrorCode.RESERVATION_ALREADY_STARTED, exception.getErrorCode());
        then(reservationMapper).should(never()).findSlotIdsByReservationId(1L);
        then(reservationMapper).should(never()).cancelReservation(1L);
    }

    @Test
    void refusesToCancelConfirmedReservationAfterUse() {
        given(reservationMapper.findById(1L)).willReturn(reservation(ReservationStatus.CONFIRMED,
                LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(1)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(1L, 10L));

        assertEquals(ReservationErrorCode.RESERVATION_ALREADY_STARTED, exception.getErrorCode());
        then(reservationMapper).should(never()).findSlotIdsByReservationId(1L);
        then(reservationMapper).should(never()).cancelReservation(1L);
    }

    @Test
    void cancelsPendingReservationRegardlessOfCheckInTime() {
        given(reservationMapper.findById(1L)).willReturn(reservation(ReservationStatus.PENDING,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)));
        given(reservationMapper.cancelReservation(1L)).willReturn(1);

        reservationService.cancelReservation(1L, 10L);

        then(reservationMapper).should().cancelReservation(1L);
    }

    @Test
    void refusesToCancelAnotherUsersReservation() {
        given(reservationMapper.findById(1L)).willReturn(reservation(ReservationStatus.PENDING,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4)));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(1L, 11L));

        assertEquals(AuthErrorCode.FORBIDDEN, exception.getErrorCode());
        then(reservationMapper).should(never()).cancelReservation(1L);
    }

    private Reservation reservation(ReservationStatus status, LocalDateTime checkIn, LocalDateTime checkOut) {
        return Reservation.builder()
                .id(1L)
                .userId(10L)
                .status(status)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();
    }

    private PlaceSlotResponse slot(Long id, LocalDateTime startAt) {
        return PlaceSlotResponse.builder()
                .slotId(id)
                .startAt(startAt)
                .endAt(startAt.plusHours(3))
                .status("OPEN")
                .build();
    }

    private List<PlaceSlotResponse> packageBoundarySlots(LocalDate checkInDate) {
        List<PlaceSlotResponse> slots = new ArrayList<>();
        long slotId = 1L;
        for (int day = 0; day < 2; day++) {
            for (int hour = 0; hour < 24; hour += 3) {
                boolean open = (day == 0 && hour >= 15) || (day == 1 && hour < 12);
                LocalDateTime startAt = checkInDate.plusDays(day).atTime(hour, 0);
                slots.add(PlaceSlotResponse.builder()
                        .slotId(slotId++)
                        .startAt(startAt)
                        .endAt(startAt.plusHours(3))
                        .status(open ? "OPEN" : "BLOCKED")
                        .build());
            }
        }
        return slots;
    }
}
