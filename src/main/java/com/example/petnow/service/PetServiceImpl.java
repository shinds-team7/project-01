package com.example.petnow.service;

import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
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
                .birthYear(request.getBirthYear())
                .sex(request.getSex())
                .weight(request.getWeight())
                .isNeutered(request.getIsNeutered())
                .memo(request.getMemo())
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
    public void updatePet(Long userId, PetUpdateRequest request){
        request.setUserId(userId);
        petMapper.updatePet(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PetDetailResponse getDetail(Long userId, Long petId){
        return petMapper.getDetail(userId, petId);
    }

    @Override
    public void deletePet(Long userId, Long petId){
        petMapper.deletePet(userId, petId);
    }
}
