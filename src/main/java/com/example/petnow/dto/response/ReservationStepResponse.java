package com.example.petnow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.petnow.entity.ReservationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationStepResponse {
    private String step;
    private List<PlaceSlotResponse> slots;
    private PlaceSlotResponse startSlot;
    private List<PackageDayResponse> days;
    private PackageDayResponse startDay;
    private ReservationType reservationType;
    private String selectedDate;
    private String errorMessage;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
}
