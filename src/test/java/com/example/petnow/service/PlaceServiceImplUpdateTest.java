package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceUpdateRequest;
import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.common.storage.ImageCategory;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlacePhoto;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.PlacePhotoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PlaceServiceImplUpdateTest {

    private PlaceMapper placeMapper;
    private PlaceGeocodingService placeGeocodingService;
    private FileStorage fileStorage;
    private PlacePhotoMapper placePhotoMapper;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        placeGeocodingService = mock(PlaceGeocodingService.class);
        fileStorage = mock(FileStorage.class);
        placePhotoMapper = mock(PlacePhotoMapper.class);
        placeService = new PlaceServiceImpl(
                placeMapper,
                mock(AuthMapper.class),
                mock(PetMapper.class),
                placeGeocodingService,
                mock(BookmarkMapper.class),
                fileStorage,
                placePhotoMapper
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

    @Test
    void appendsNewPhotoAfterExistingPhotos() {
        given(placeMapper.findById(3L)).willReturn(ownedPlace());
        PlaceUpdateRequest request = request();
        given(placeMapper.update(3L, 1L, request, null)).willReturn(1);
        MockMultipartFile image = new MockMultipartFile(
                "images", "yard.jpg", "image/jpeg", new byte[]{1});
        request.setImages(List.of(image));
        given(placePhotoMapper.countByPlaceId(3L)).willReturn(2);
        given(placePhotoMapper.findNextSortOrder(3L)).willReturn(2);
        given(fileStorage.uploadImage(image, ImageCategory.PLACE)).willReturn("/uploads/places/yard.jpg");

        placeService.updatePlace(1L, 3L, request);

        verify(placePhotoMapper).insertPhoto(org.mockito.ArgumentMatchers.argThat(photo ->
                photo.getPlaceId().equals(3L)
                        && photo.getImageUrl().equals("/uploads/places/yard.jpg")
                        && photo.getSortOrder().equals(2)));
    }

    @Test
    void deletesOnlyPhotoBelongingToOwnedPlace() {
        given(placeMapper.findById(3L)).willReturn(ownedPlace());
        PlacePhoto photo = PlacePhoto.builder()
                .id(9L).placeId(3L).imageUrl("/uploads/places/old.jpg").sortOrder(0).build();
        given(placePhotoMapper.findByIdAndPlaceId(9L, 3L)).willReturn(photo);
        given(placePhotoMapper.deleteById(9L)).willReturn(1);

        placeService.deletePlacePhoto(1L, 3L, 9L);

        verify(placePhotoMapper).deleteById(9L);
        verify(fileStorage).deleteImage("/uploads/places/old.jpg");
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
