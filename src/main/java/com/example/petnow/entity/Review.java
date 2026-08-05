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
    private Long reservationId;     // FK
    private Integer rating;
    private String content;
    private boolean isReadByHost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
