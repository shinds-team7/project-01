package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService{

    private final PlaceMapper placeMapper;

    @Override
    @Transactional
    public void createPlace(Long userId, PlaceCreateRequest request) {
        Place place = request.toEntity(userId);

        int result = placeMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new BusinessException(ErrorCode.PLACE_CREATE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceListResponse> getPlacesByUserId(Long userId) {
        return placeMapper.findAllByUserId(userId);
    }
}
