package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.User;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private final PlaceMapper placeMapper;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public void createPlace(Long userId, PlaceCreateRequest request) {
        User user = authMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        Place place = Place.builder()
                .hostUserId(userId)
                .hostUserNickname(user.getNickname())
                .name(request.getName())
                .description(request.getDescription())
                .placeType(request.getPlaceType())
                .areaSize(request.getAreaSize())
                .capacity(request.getCapacity())
                .allowsSmallDog(request.isAllowsSmallDog())
                .allowsMediumDog(request.isAllowsMediumDog())
                .allowsLargeDog(request.isAllowsLargeDog())
                .providesHomeCamera(request.isProvidesHomeCamera())
                .providesRealtimePhoto(request.isProvidesRealtimePhoto())
                .providesYard(request.isProvidesYard())
                .providesWalk(request.isProvidesWalk())
                .otherOptions(request.isOtherOptionsEnabled() ? request.getOtherOptions() : null)
                .hourlyPrice(request.getHourlyPrice())
                .nightlyPrice(request.getNightlyPrice())
                .status(PlaceStatus.PUBLISHED)
                .build();

        int result = placeMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_CREATE_FAILED);
        }
    }

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
