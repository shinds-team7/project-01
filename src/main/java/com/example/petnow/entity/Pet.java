package com.example.petnow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * pets 테이블 매핑. 필드명은 ERD 컬럼명 기준.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    private Long petId;

    private Long userId;

    private String name;

    private Double weight;

    private String breed;

    /** 'MALE' / 'FEMALE' */
    private String sex;

    private Boolean isNeutered;

    /** 'SMALL' / 'MEDIUM' / 'LARGE' */
    private String size;

    private Integer birthYear;

    private String memo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    /** 연관 관계. pets 테이블 컬럼이 아님 */
    private List<PetPhoto> photos;
}
