package com.example.petnow.mapper;

import com.example.petnow.entity.PetPhoto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface PetPhotoMapper {

    void insertPhoto(PetPhoto photo);

    List<PetPhoto> findByPetId(Long petId);

    void deleteByPetId(Long petId);

}
