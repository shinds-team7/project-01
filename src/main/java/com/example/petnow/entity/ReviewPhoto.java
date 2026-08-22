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
public class ReviewPhoto {

    private Long id;

    private Long reviewId;

    private String imageUrl;

    private Integer sortOrder;

    private LocalDateTime createdAt;

}
