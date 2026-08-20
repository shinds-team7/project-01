package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceUpdateRequest;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PlaceServiceImplUpdateTest {

    private PlaceMapper placeMapper;
    private PlaceGeocodingService placeGeocodingService;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        placeGeocodingService = mock(PlaceGeocodingService.class);
        // 이 테스트는 장소 수정만 본다. 찜 여부는 상세 조회에서만 쓰여 목으로 자리만 채운다.
        placeService = new PlaceServiceImpl(
                placeMapper,
                mock(AuthMapper.class),
                mock(PetMapper.class),
                placeGeocodingService,
                mock(BookmarkMapper.class)
        );
    }

    @Test
    void updatesOwnedPlaceAndAddressInOneServiceCall() {
        given(placeMapper.findById(3L)).willReturn(ownedPlace());
        PlaceUpdateRequest request = request();
        given(placeMapper.update(3L, 1L, request, null)).willReturn(1);

        placeService.updatePlace(1L, 3L, request);

        verify(placeMapper).update(3L, 1L, request, null);
        verify(placeMapper).upsertAddress(
                3L, "서울특별시", "성동구", "서울특별시 성동구 왕십리로 1");
        verify(placeGeocodingService).geocodeAndUpdate(
                3L, "서울특별시 성동구 왕십리로 1");
    }

    @Test
    void rejectsUpdateByAnotherHost() {
        given(placeMapper.findById(3L)).willReturn(ownedPlace());

        assertThatThrownBy(() -> placeService.updatePlace(99L, 3L, request()))
                .isInstanceOf(BusinessException.class);

        verify(placeMapper, never()).update(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private Place ownedPlace() {
        return Place.builder().id(3L).hostUserId(1L).build();
    }

    private PlaceUpdateRequest request() {
        PlaceUpdateRequest request = new PlaceUpdateRequest();
        request.setSigungu("성동구");
        request.setRoadAddress("서울특별시 성동구 왕십리로 1");
        return request;
    }
}
