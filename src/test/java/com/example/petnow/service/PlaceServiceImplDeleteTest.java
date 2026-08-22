package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.PlacePhotoMapper;
import com.example.petnow.mapper.ReservationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 호스트 장소 삭제 정책을 고정한다. (host/dashboard.html 의 비활성 삭제 버튼 대체)
 */
class PlaceServiceImplDeleteTest {

    private PlaceMapper placeMapper;
    private ReservationMapper reservationMapper;
    private PlaceServiceImpl placeService;

    @BeforeEach
    void setUp() {
        placeMapper = mock(PlaceMapper.class);
        reservationMapper = mock(ReservationMapper.class);
        placeService = new PlaceServiceImpl(
                placeMapper,
                mock(AuthMapper.class),
                mock(PetMapper.class),
                mock(PlaceGeocodingService.class),
                mock(BookmarkMapper.class),
                mock(FileStorage.class),
                mock(PlacePhotoMapper.class),
                reservationMapper);

        given(placeMapper.findById(3L)).willReturn(Place.builder().id(3L).hostUserId(1L).build());
    }

    @Test
    @DisplayName("대기·확정 예약이 없으면 소프트 삭제한다")
    void softDeletesWhenNoActiveReservations() {
        given(reservationMapper.countActiveByPlaceId(3L)).willReturn(0);
        given(placeMapper.softDelete(3L, 1L)).willReturn(1);

        placeService.deletePlace(1L, 3L);

        verify(placeMapper).softDelete(3L, 1L);
    }

    @Test
    @DisplayName("대기·확정 예약이 있으면 삭제 없이 예외를 던진다")
    void rejectsDeleteWhenActiveReservationsExist() {
        given(reservationMapper.countActiveByPlaceId(3L)).willReturn(1);

        assertThatThrownBy(() -> placeService.deletePlace(1L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PlaceErrorCode.PLACE_HAS_ACTIVE_RESERVATIONS);

        verify(placeMapper, never()).softDelete(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("남의 장소는 삭제할 수 없다")
    void rejectsDeleteByNonOwner() {
        assertThatThrownBy(() -> placeService.deletePlace(99L, 3L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PlaceErrorCode.PLACE_ACCESS_DENIED);
    }
}
