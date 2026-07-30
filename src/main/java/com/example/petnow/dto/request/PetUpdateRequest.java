package com.example.petnow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PetUpdateRequest {
    private Long petId;
    private Double weight;
    private Boolean neutered;
}
