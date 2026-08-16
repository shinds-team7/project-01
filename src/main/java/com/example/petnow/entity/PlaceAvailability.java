package com.example.petnow.entity;

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
public class PlaceAvailability {
    private Long id;
    private Long placeId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private SlotStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
