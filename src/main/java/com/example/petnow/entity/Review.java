package com.example.petnow.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private Long id;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long reservationId;     // FK
}
