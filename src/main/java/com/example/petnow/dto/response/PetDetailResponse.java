package com.example.petnow.dto.response;

import com.example.petnow.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDetailResponse {

    private String name;
    private Pet.Size size;
    private Double weight;
    private Integer birthYear;
    private String sex;
    private Boolean isNeutered;
    private String memo;
}
