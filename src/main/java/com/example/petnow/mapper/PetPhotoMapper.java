package com.example.petnow.mapper;

import com.example.petnow.entity.PetPhoto;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface PetPhotoMapper {

    void insertPhoto(PetPhoto photo);

    PetPhoto findByPetId(Long petId);

    void deleteByPetId(Long petId);

}
