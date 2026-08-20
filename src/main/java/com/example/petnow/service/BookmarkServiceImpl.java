package com.example.petnow.service;

import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkMapper bookmarkMapper;
    private final PlaceMapper placeMapper;

    @Override
    @Transactional
    public boolean toggle(Long userId, Long placeId) {
        validatePublicPlace(placeId);

        if (bookmarkMapper.existsByUserAndPlace(userId, placeId)) {
            bookmarkMapper.delete(userId, placeId);
            return false;
        }

        try {
            bookmarkMapper.insert(userId, placeId);
        } catch (DuplicateKeyException ignored) {
            return true;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userId, Long placeId) {
        return userId != null && bookmarkMapper.existsByUserAndPlace(userId, placeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceListResponse> getBookmarkedPlaces(Long userId) {
        return bookmarkMapper.findPlacesByUserId(userId);
    }

    private void validatePublicPlace(Long placeId) {
        PlaceDetailResponse place = placeMapper.findDetailById(placeId);
        if (place == null
                || place.getStatus() != PlaceStatus.PUBLISHED
                || !place.isVisible()) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
    }
}
