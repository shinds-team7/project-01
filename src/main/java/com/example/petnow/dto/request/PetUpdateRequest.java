package com.example.petnow.dto.request;

import com.example.petnow.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PetUpdateRequest {
    private Long petId;
    private String name;
    private Double weight;
    private Pet.SizeCode sizeCode;
    private Boolean neutered;
    private String note;
}
