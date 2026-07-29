package com.example.petnow.service;

import com.example.petnow.dto.PlaceCreateRequestDTO;
import com.example.petnow.entity.Place;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService{

    private final PlaceMapper placeMapper;
    @Override
    @Transactional
    public Long createPlace(Long userId, PlaceCreateRequestDTO requestDTO) {

        if (requestDTO.getTitle() == null || requestDTO.getTitle().isBlank()) {
            throw new IllegalArgumentException("장소명을 입력해주세요.");
        }

        if(requestDTO.getMaxDogCount() <= 0) {
            throw new IllegalArgumentException("최대 마릿 수는 1 이상이어야 합니다.");
        }

        if (!requestDTO.isOptionEtc()) {
            requestDTO.setOptionText(null);
        }
        Place place = Place.builder()
                .userId(userId)
                .title(requestDTO.getTitle())
                .address(requestDTO.getAddress())
                .detailAddress(requestDTO.getDetailAddress())
                .info(requestDTO.getInfo())
                .placeType(requestDTO.getPlaceType())
                .placeArea(requestDTO.getPlaceArea())
                .maxDogCount(requestDTO.getMaxDogCount())
                .optionSmallDog(requestDTO.isOptionSmallDog())
                .optionMediumDog(requestDTO.isOptionMediumDog())
                .optionLargeDog(requestDTO.isOptionLargeDog())
                .optionHomeCam(requestDTO.isOptionHomeCam())
                .optionRealTimePhoto(requestDTO.isOptionRealTimePhoto())
                .optionYard(requestDTO.isOptionYard())
                .optionWalkService(requestDTO.isOptionWalkService())
                .optionEtc(requestDTO.isOptionEtc())
                .optionText(requestDTO.getOptionText())
                .hourPrice(requestDTO.getHourPrice())
                .dayPrice(requestDTO.getDayPrice())
                .build();


        int result = placeMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new IllegalStateException("장소 등록에 실패했습니다.");
        }


        return place.getId();
    }
}
