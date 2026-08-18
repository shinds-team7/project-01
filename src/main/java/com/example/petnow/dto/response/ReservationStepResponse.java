package com.example.petnow.dto.response;

import java.math.BigDecimal;
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
    /** 확인 단계에서 결제 금액을 미리 보여주기 위한 값. 가격이 설정되지 않은 장소면 null 이다. */
    private BigDecimal totalPrice;
}
