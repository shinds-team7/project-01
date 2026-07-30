package com.example.petnow.dto.request;

import com.example.petnow.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PetCreateRequest {

    private String name;

    private Integer birthYear;

    private String gender;

    private Double weight;

    private Boolean neutered;

    private String note;

    private Pet.SizeCode sizeCode;

    private String photoUrl;

}