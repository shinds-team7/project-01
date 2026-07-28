package com.example.petnow.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetPhoto {

    private Long id;

    private Long petId;

    private String photoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}