package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.entity.User;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.PlacePhotoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlaceServiceImplCreatePlaceTest {

    private PlaceMapper placeMapper;
    private AuthMapper authMapper;
    private PlaceGeocodingService placeGeocodingService;
    private FileStorage fileStorage;
    private PlacePhotoMapper placePhotoMapper;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        authMapper = mock(AuthMapper.class);
        placeGeocodingService = mock(PlaceGeocodingService.class);
        fileStorage = mock(FileStorage.class);
        placePhotoMapper = mock(PlacePhotoMapper.class);
        placeService = new PlaceServiceImpl(
                placeMapper, authMapper, mock(PetMapper.class), placeGeocodingService,
                mock(BookmarkMapper.class), fileStorage, placePhotoMapper);
    }

    @Test
    @DisplayName("장소 생성 직후 서울특별시 상수와 요청 주소를 저장한다")
    void createsAddressAfterPlace() {
        given(authMapper.findById(7L)).willReturn(User.builder()
                .email("host@petnow.kr")
                .nickname("호스트")
                .password("password")
                .build());
        given(placeMapper.insert(any(Place.class))).willAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            place.setId(41L);
            return 1;
        });
        given(placeMapper.insertAddress(
                41L,
                "서울특별시",
                "성동구",
                "서울특별시 성동구 왕십리로 83-21"))
                .willReturn(1);

        placeService.createPlace(7L, request());
        verify(placeMapper).insertAddress(
                41L,
                "서울특별시",
                "성동구",
                "서울특별시 성동구 왕십리로 83-21");
        verify(placeGeocodingService)
                .geocodeAndUpdate(41L, "서울특별시 성동구 왕십리로 83-21");
    }

    private PlaceCreateRequest request() {
        PlaceCreateRequest request = new PlaceCreateRequest();
        request.setName("성수 조용한 단독주택");
        request.setDescription("반려견이 편하게 쉴 수 있는 공간입니다.");
        request.setSigungu("성동구");
        request.setRoadAddress("서울특별시 성동구 왕십리로 83-21");
        request.setPlaceType(PlaceType.HOUSE);
        request.setAreaSize(new BigDecimal("42"));
        request.setCapacity(2);
        request.setHourlyPrice(new BigDecimal("12000"));
        request.setNightlyPrice(new BigDecimal("48000"));
        return request;
    }
}
