package com.example.petnow.dto.request;

import com.example.petnow.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class PetCreateRequest {

    private String name;

    private Integer birthYear;

    private String sex;

    private Double weight;

    private Boolean isNeutered;

    private String memo;

    private Pet.Size size;

    private MultipartFile image;

}
