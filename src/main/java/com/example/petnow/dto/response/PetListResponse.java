package com.example.petnow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetListResponse {

    private Long petId;
    private String name;
    private String sizeCode;
    private Double weight;

}
