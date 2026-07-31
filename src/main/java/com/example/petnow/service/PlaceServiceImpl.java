package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequestDTO;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ErrorCode;
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
    public void createPlace(Long userId, PlaceCreateRequestDTO requestDTO) {
        Place place = requestDTO.toEntity(userId);

        int result = placeMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new BusinessException(ErrorCode.PLACE_CREATE_FAILED);
        }
    }
}
