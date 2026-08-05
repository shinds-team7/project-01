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
public class Pet {

    private Long id;

    private Long userId;

    private String name;

    private String breed;

    private Integer birthYear;

    private String sex;

    private Double weight;

    private Boolean neutered;

    private String note;

    private Size size;

    private PetPhoto photo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Size {
        SMALL("소형"), MEDIUM("중형"), LARGE("대형");

        private final String label;

        Size(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}