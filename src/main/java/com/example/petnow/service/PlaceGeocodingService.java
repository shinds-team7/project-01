package com.example.petnow.service;

import com.example.petnow.entity.PlaceAddress;
import com.example.petnow.mapper.PlaceAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceGeocodingService {

    private static final int MIN_BACKFILL_BATCH_SIZE = 1;
    private static final int MAX_BACKFILL_BATCH_SIZE = 1_000;

    private final GeocodingClient geocodingClient;
    private final PlaceAddressMapper placeAddressMapper;

    /**
     * 외부 API 실패가 장소 등록을 막지 않도록 좌표 저장 결과만 반환한다.
     */
    public boolean geocodeAndUpdate(Long placeId, String roadAddress) {
        try {
            Optional<GeocodedCoordinates> coordinates = geocodingClient.geocode(roadAddress);
            if (coordinates.isEmpty()) {
                log.warn("주소의 좌표를 찾지 못했습니다. placeId={}", placeId);
                return false;
            }

            GeocodedCoordinates value = coordinates.get();
            int updated = placeAddressMapper.updateCoordinates(
                    placeId, value.latitude(), value.longitude());
            if (updated != 1) {
                log.warn("장소 좌표 저장 대상이 없거나 중복되었습니다. placeId={}, updated={}", placeId, updated);
                return false;
            }
            return true;
        } catch (RuntimeException exception) {
            log.warn("장소 좌표 저장에 실패했지만 장소 등록은 계속합니다. placeId={}", placeId, exception);
            return false;
        }
    }

    public BackfillResult backfillMissingCoordinates(int requestedBatchSize) {
        int batchSize = Math.max(
                MIN_BACKFILL_BATCH_SIZE,
                Math.min(requestedBatchSize, MAX_BACKFILL_BATCH_SIZE));
        List<PlaceAddress> targets = placeAddressMapper.findWithoutCoordinates(batchSize);
        int updated = 0;
        for (PlaceAddress target : targets) {
            if (geocodeAndUpdate(target.getPlaceId(), target.getRoadAddress())) {
                updated++;
            }
        }
        return new BackfillResult(targets.size(), updated, targets.size() - updated);
    }

    public record BackfillResult(int attempted, int updated, int failed) {
    }
}
