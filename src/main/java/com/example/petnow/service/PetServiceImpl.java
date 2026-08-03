package com.example.petnow.service;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.PetPhoto;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PetPhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetPhotoMapper petPhotoMapper;

    @Override
    public void createPet(Long userId, PetCreateRequest request) {

        // Pet Entity 생성
        Pet pet = Pet.builder()
                .userId(userId)
                .name(request.getName())
                .birthYear(request.getBirthYear())
                .gender(request.getGender())
                .weight(request.getWeight())
                .neutered(request.getNeutered())
                .note(request.getNote())
                .sizeCode(request.getSizeCode())
                .build();

        // pets 테이블 저장
        petMapper.insertPet(pet);
    }
}