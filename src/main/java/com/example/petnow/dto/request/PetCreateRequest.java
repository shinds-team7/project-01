package com.example.petnow.dto.request;

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

    private String sizeCode;

    private String photoUrl;

}