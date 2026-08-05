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
public class PetListResponse {
    private String name;
    private Pet.Size size;
    private Double weight;
}
