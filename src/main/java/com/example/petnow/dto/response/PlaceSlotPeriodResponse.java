package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSlotPeriodResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
}
