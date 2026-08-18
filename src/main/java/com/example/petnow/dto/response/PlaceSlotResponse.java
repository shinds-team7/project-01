package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSlotResponse {
    private Long slotId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
}
