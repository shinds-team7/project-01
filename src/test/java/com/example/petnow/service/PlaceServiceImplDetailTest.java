package com.example.petnow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.PlacePhotoMapper;
import com.example.petnow.mapper.ReservationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaceServiceImplDetailTest {

    private PlaceMapper placeMapper;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        placeService = new PlaceServiceImpl(
                placeMapper,
                mock(AuthMapper.class),
                mock(PetMapper.class),
                mock(PlaceGeocodingService.class),
                mock(BookmarkMapper.class),
                mock(FileStorage.class),
                mock(PlacePhotoMapper.class),
                mock(ReservationMapper.class));
    }

    @Test
    @DisplayName("장소 상세 공개 주소는 도로명을 우선한다")
    void detailPrefersRoadAddress() {
        PlaceDetailResponse place = publicPlace();
        place.setSido("서울특별시");
        place.setSigungu("성동구");
        place.setEupmyeondong("성수동");
        place.setRoadAddress("  서울특별시 성동구 왕십리로 1  ");
        given(placeMapper.findDetailById(3L)).willReturn(place);

        PlaceDetailResponse result = placeService.getPlaceDetail(3L, null);

        assertThat(result.getAddress()).isEqualTo("서울특별시 성동구 왕십리로 1");
    }

    @Test
    @DisplayName("도로명이 없으면 시도·시군구·읍면동으로 공개 주소를 만든다")
    void detailFallsBackToAdministrativeAddress() {
        PlaceDetailResponse place = publicPlace();
        place.setSido("서울특별시");
        place.setSigungu("성동구");
        place.setEupmyeondong("성수동");
        given(placeMapper.findDetailById(3L)).willReturn(place);

        PlaceDetailResponse result = placeService.getPlaceDetail(3L, null);

        assertThat(result.getAddress()).isEqualTo("서울특별시 성동구 성수동");
    }

    @Test
    @DisplayName("주소 정보가 전혀 없으면 공개 주소도 null이다")
    void detailKeepsMissingAddressNull() {
        PlaceDetailResponse place = publicPlace();
        given(placeMapper.findDetailById(3L)).willReturn(place);

        PlaceDetailResponse result = placeService.getPlaceDetail(3L, null);

        assertThat(result.getAddress()).isNull();
    }

    private PlaceDetailResponse publicPlace() {
        PlaceDetailResponse place = new PlaceDetailResponse();
        place.setId(3L);
        place.setStatus(PlaceStatus.PUBLISHED);
        place.setVisible(true);
        return place;
    }
}
