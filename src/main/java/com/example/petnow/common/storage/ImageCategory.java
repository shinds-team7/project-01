package com.example.petnow.common.storage;

import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이미지를 올리는 도메인과 그 도메인의 업로드 정책.
 *
 * <p>저장 경로 접두사와 최대 장수를 도메인마다 따로 두지 않고 여기 한 곳에 모은다.
 * "장소 사진을 15장까지 허용한다" 같은 정책 변경은 이 파일만 고치면 끝난다.
 */
@Getter
@RequiredArgsConstructor
public enum ImageCategory {

    USER("users", 1),
    PET("pets", 1),
    PLACE("places", 10),
    REVIEW("reviews", 5);

    private final String prefix;
    private final int maxCount;

    /**
     * 이 도메인이 허용하는 장수를 넘겼는지 검사한다.
     *
     * <p>도메인 서비스에서 직접 개수를 비교하면 도메인마다 기준이 어긋나므로 정책을 가진 쪽에서 판단한다.
     */
    public void validateCount(int count) {
        if (count > maxCount) {
            throw new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED,
                    "이미지는 최대 %d장까지 올릴 수 있습니다".formatted(maxCount));
        }
    }
}
