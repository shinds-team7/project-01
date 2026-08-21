package com.example.petnow.dto.request;

import com.example.petnow.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class PetUpdateRequest {
    private Long userId;
    private Long petId;
    private String name;
    private Integer birthYear;
    private String sex;
    private Double weight;
    private Pet.Size size;
    private Boolean isNeutered;
    private String memo;
    private MultipartFile image;
}
