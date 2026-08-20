package com.example.petnow.service;

import com.example.petnow.common.config.KakaoLocalApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "kakao.local-api",
        name = "backfill-enabled",
        havingValue = "true")
public class PlaceCoordinateBackfillRunner implements ApplicationRunner {

    private final PlaceGeocodingService placeGeocodingService;
    private final KakaoLocalApiProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        PlaceGeocodingService.BackfillResult result = placeGeocodingService
                .backfillMissingCoordinates(properties.getBackfillBatchSize());
        log.info(
                "장소 좌표 백필을 마쳤습니다. attempted={}, updated={}, failed={}",
                result.attempted(), result.updated(), result.failed());
    }
}
