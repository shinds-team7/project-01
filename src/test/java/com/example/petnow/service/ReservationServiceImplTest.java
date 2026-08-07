package com.example.petnow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private PlaceMapper placeMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        Place place = Place.builder()
                .hourlyPrice(BigDecimal.valueOf(12_000))
                .nightlyPrice(BigDecimal.valueOf(48_000))
                .build();
        when(placeMapper.findById(1L)).thenReturn(place);
        doAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(100L);
            return null;
        }).when(reservationMapper).save(any(Reservation.class));
    }

    @Test
    void savesDayUseTypeAndHourlyPriceForSameDayReservation() {
        ReservationRequest request = reservationRequest(
                LocalDateTime.of(2026, 8, 9, 13, 0),
                LocalDateTime.of(2026, 8, 9, 18, 0));

        // develop 에서 반환값이 예약 ID(Long) 에서 예약번호(String) 로 바뀌었다.
        String reservationNo = reservationService.saveReservation(request, 10L);

        Reservation saved = captureSavedReservation();
        assertThat(reservationNo).isNotBlank();
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getReservationType()).isEqualTo("당일");
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("60000");
    }

    @Test
    void savesOvernightTypeAndNightlyPriceForMultiDayReservation() {
        ReservationRequest request = reservationRequest(
                LocalDateTime.of(2026, 8, 9, 18, 0),
                LocalDateTime.of(2026, 8, 11, 10, 0));

        reservationService.saveReservation(request, 10L);

        Reservation saved = captureSavedReservation();
        assertThat(saved.getReservationType()).isEqualTo("숙박");
        assertThat(saved.getTotalPrice()).isEqualByComparingTo("96000");
    }

    private ReservationRequest reservationRequest(LocalDateTime checkIn, LocalDateTime checkOut) {
        return ReservationRequest.builder()
                .placeId(1L)
                .petIds(List.of(2L))
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();
    }

    private Reservation captureSavedReservation() {
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationMapper).save(captor.capture());
        return captor.getValue();
    }
}
