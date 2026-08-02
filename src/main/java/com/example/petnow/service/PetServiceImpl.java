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

        // Pet Entity 생성 (요청 DTO 필드명 -> ERD 컬럼명)
        Pet pet = Pet.builder()
                .userId(userId)
                .name(request.getName())
                .birthYear(request.getBirthYear())
                .sex(request.getGender())
                .weight(request.getWeight())
                .isNeutered(request.getNeutered())
                .memo(request.getNote())
                .size(request.getSizeCode())
                .build();

        // pets 테이블 저장
        petMapper.insertPet(pet);
    }
}