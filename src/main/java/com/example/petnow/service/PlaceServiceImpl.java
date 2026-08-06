package com.example.petnow.service;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceMapper placeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlaceListResponse> getPublishedPlaces() {
        return placeMapper.findAllPublished();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId) {
        PlaceDetailResponse place = placeMapper.findDetailById(placeId);

        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        boolean owner = loginUserId != null && loginUserId.equals(place.getHostUserId());
        boolean publiclyAccessible = place.isVisible()
                && place.getStatus() == PlaceStatus.PUBLISHED;

        if (!owner && !publiclyAccessible) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        return place;
    }
}
