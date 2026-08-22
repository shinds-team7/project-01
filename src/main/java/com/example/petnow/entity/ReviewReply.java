package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReply {

    private Long id;

    private Long reviewId;

    private Long hostUserId;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
