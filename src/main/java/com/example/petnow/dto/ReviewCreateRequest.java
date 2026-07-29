package com.example.petnow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {
    private Integer score;
    private String comment;
    private Long reservationId;
}
