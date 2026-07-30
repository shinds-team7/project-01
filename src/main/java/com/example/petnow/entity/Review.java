package com.example.petnow.entity;

import com.example.petnow.common.domain.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {
    private Long id;
    private Integer score;
    private String comment;
    private Long reservationId;     // FK
}
