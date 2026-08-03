package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    private Long id;

    private Long userId;

    private String name;

    private Integer birthYear;

    private String gender;

    private Double weight;

    private Boolean neutered;

    private String note;

    private String sizeCode;

    private PetPhoto photo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}