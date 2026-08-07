package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.HostPlaceListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.HostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HostServiceImpl implements HostService{

    private final HostMapper hostMapper;

    @Override
    @Transactional
    public void createPlace(Long userId, PlaceCreateRequest request) {
        Place place = request.toEntity(userId);

        int result = hostMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_CREATE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostPlaceListResponse> getPlacesByUserId(Long userId) {
        return hostMapper.findAllByUserId(userId);
    }
}
