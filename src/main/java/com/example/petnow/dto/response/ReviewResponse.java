package com.example.petnow.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponse {

    private Long id;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

//    private String placeName;
//    private String writer;
}
