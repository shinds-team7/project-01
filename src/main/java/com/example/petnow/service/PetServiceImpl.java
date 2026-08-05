package com.example.petnow.service;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PetPhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .breed(request.getBreed())
                .birthYear(request.getBirthYear())
                .sex(request.getSex())
                .weight(request.getWeight())
                .neutered(request.getNeutered())
                .note(request.getNote())
                .size(request.getSize())
                .build();

        // pets 테이블 저장
        petMapper.insertPet(pet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetListResponse> getPetList(Long userId) {

        return petMapper.getPetList(userId);
    }

    @Override
    public void updatePet(Long petId, PetUpdateRequest request){
        request.setPetId(petId);
        petMapper.updatePet(request);
    }
}