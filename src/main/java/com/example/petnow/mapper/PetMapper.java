package com.example.petnow.mapper;

import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PetMapper {

    void insertPet(Pet pet);

    List<PetListResponse> getPetList(Long userId);

    void updatePet(PetUpdateRequest pet);
}