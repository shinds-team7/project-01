package com.example.petnow.mapper;

import com.example.petnow.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PetMapper {

    void insertPet(Pet pet);

//    List<Pet> findByUserId(Long userId);

//    Pet findByPetId(Long petId);
//
//    void updatePet(Pet pet);
//
//    void deletePet(Long petId);

}
