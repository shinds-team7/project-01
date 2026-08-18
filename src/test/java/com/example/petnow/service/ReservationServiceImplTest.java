package com.example.petnow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petnow.dto.request.ReservationRequest;
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
}
