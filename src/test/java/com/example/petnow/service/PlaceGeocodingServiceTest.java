package com.example.petnow.service;

import com.example.petnow.entity.PlaceAddress;
import com.example.petnow.mapper.PlaceAddressMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PlaceGeocodingServiceTest {

    private GeocodingClient geocodingClient;
    private PlaceAddressMapper placeAddressMapper;
    private PlaceGeocodingService service;

    @BeforeEach
    void setUp() {
        geocodingClient = mock(GeocodingClient.class);
        placeAddressMapper = mock(PlaceAddressMapper.class);
        service = new PlaceGeocodingService(geocodingClient, placeAddressMapper);
    }

    @Test
    void savesCoordinatesWhenGeocodingSucceeds() {
        GeocodedCoordinates coordinates = new GeocodedCoordinates(
                new BigDecimal("37.5446397"),
                new BigDecimal("127.0557550"));
        given(geocodingClient.geocode("서울특별시 성동구 왕십리로 83-21"))
                .willReturn(Optional.of(coordinates));
        given(placeAddressMapper.updateCoordinates(
                41L, coordinates.latitude(), coordinates.longitude()))
                .willReturn(1);

        boolean updated = service.geocodeAndUpdate(
                41L, "서울특별시 성동구 왕십리로 83-21");

        assertThat(updated).isTrue();
        verify(placeAddressMapper).updateCoordinates(
                41L,
                new BigDecimal("37.5446397"),
                new BigDecimal("127.0557550"));
    }

    @Test
    void leavesCoordinatesEmptyWhenKakaoReturnsNoDocuments() {
        given(geocodingClient.geocode("검색되지 않는 주소"))
                .willReturn(Optional.empty());

        boolean updated = service.geocodeAndUpdate(41L, "검색되지 않는 주소");

        assertThat(updated).isFalse();
        verify(placeAddressMapper, never()).updateCoordinates(any(), any(), any());
    }

    @Test
    void doesNotPropagateGeocodingFailure() {
        given(geocodingClient.geocode("서울특별시 성동구 왕십리로 83-21"))
                .willThrow(new IllegalStateException("temporary outage"));

        boolean updated = service.geocodeAndUpdate(
                41L, "서울특별시 성동구 왕십리로 83-21");

        assertThat(updated).isFalse();
        verify(placeAddressMapper, never()).updateCoordinates(any(), any(), any());
    }

    @Test
    void backfillsOnlyMissingCoordinateTargetsInConfiguredBatch() {
        PlaceAddress first = PlaceAddress.builder()
                .placeId(41L)
                .roadAddress("서울특별시 성동구 왕십리로 83-21")
                .build();
        PlaceAddress second = PlaceAddress.builder()
                .placeId(42L)
                .roadAddress("검색되지 않는 주소")
                .build();
        GeocodedCoordinates coordinates = new GeocodedCoordinates(
                new BigDecimal("37.5446397"),
                new BigDecimal("127.0557550"));
        given(placeAddressMapper.findWithoutCoordinates(100))
                .willReturn(List.of(first, second));
        given(geocodingClient.geocode(first.getRoadAddress()))
                .willReturn(Optional.of(coordinates));
        given(placeAddressMapper.updateCoordinates(
                first.getPlaceId(), coordinates.latitude(), coordinates.longitude()))
                .willReturn(1);
        given(geocodingClient.geocode(second.getRoadAddress()))
                .willReturn(Optional.empty());

        PlaceGeocodingService.BackfillResult result = service.backfillMissingCoordinates(100);

        assertThat(result).isEqualTo(new PlaceGeocodingService.BackfillResult(2, 1, 1));
    }
}
