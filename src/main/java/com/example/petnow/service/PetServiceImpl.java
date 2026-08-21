package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.common.storage.ImageCategory;
import com.example.petnow.dto.request.PetCreateRequest;
import com.example.petnow.dto.request.PetUpdateRequest;
import com.example.petnow.dto.response.PetDetailResponse;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.PetPhoto;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PetPhotoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetPhotoMapper petPhotoMapper;
    private final FileStorage fileStorage;

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

        MultipartFile image = request.getImage();

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorage.uploadImage(
                image,
                ImageCategory.PET
            );
            PetPhoto petPhoto = PetPhoto.builder()
                .petId(pet.getId())
                .imageUrl(imageUrl)
                .sortOrder(0)
                .build();

            petPhotoMapper.insertPhoto(petPhoto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetListResponse> getPetList(Long userId) {

        return petMapper.getPetList(userId);
    }

//    @Override
//    public void updatePet(Long userId, PetUpdateRequest request){
//        request.setUserId(userId);
//        petMapper.updatePet(request);
//    }
    @Override
    public void updatePet(Long userId, PetUpdateRequest request) {

        request.setUserId(userId);

        // 반려동물 기본 정보 수정
        int updatedRows = petMapper.updatePet(request);

        // 해당 사용자의 반려동물이 아니거나 이미 삭제된 경우
        if (updatedRows == 0) {
            throw new IllegalArgumentException("수정할 반려동물을 찾을 수 없습니다.");
        }

        // 새 사진을 선택하지 않았다면 기존 사진 유지
        MultipartFile image = request.getImage();

        if (image == null || image.isEmpty()) {
            return;
        }

        // 새 사진 S3 업로드
        String imageUrl = fileStorage.uploadImage(
            image,
            ImageCategory.PET
        );

        // 기존 사진 조회
        PetPhoto existingPhoto =
            petPhotoMapper.findByPetId(request.getPetId());

        if (existingPhoto != null) {

            // 기존 S3 이미지 삭제
            fileStorage.deleteImage(existingPhoto.getImageUrl());

            // DB의 이미지 URL 변경
            existingPhoto.setImageUrl(imageUrl);
            petPhotoMapper.updatePhoto(existingPhoto);

        } else {

            // 기존 사진이 없다면 새로 등록
            PetPhoto petPhoto = PetPhoto.builder()
                .petId(request.getPetId())
                .imageUrl(imageUrl)
                .sortOrder(0)
                .build();

            petPhotoMapper.insertPhoto(petPhoto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PetDetailResponse getDetail(Long userId, Long petId){
        return petMapper.getDetail(userId, petId);
    }

    @Override
    public void deletePet(Long userId, Long petId){

        petMapper.deletePet(userId, petId);
        // 사진 조회 후 삭제
        PetPhoto existingPhoto = petPhotoMapper.findByPetId(petId);
        if (existingPhoto != null) fileStorage.deleteImage(existingPhoto.getImageUrl());
        petPhotoMapper.deleteByPetId(petId);
    }
}
