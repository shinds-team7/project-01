package com.example.petnow.dto.response;

import com.example.petnow.entity.PlaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceListResponse {

    private Long id;
    private String nickname;
    private String name;
    private String description;
    private PlaceType placeType;
    private boolean allowsSmallDog;
    private boolean allowsMediumDog;
    private boolean allowsLargeDog;
    private boolean providesHomeCamera;
    private boolean providesRealtimePhoto;
    private boolean providesYard;
    private boolean providesWalk;
    private BigDecimal hourlyPrice;
    private boolean bookmarked;

    /**
     * 장소 카드에 띄우는 평균 별점. {@code places.average_rating} 을 그대로 읽는다. (#182 의 함정 1 해소)
     *
     * <p>리뷰 생성·수정·삭제마다 {@code PlaceMapper.updateAvgRating} 이 다시 계산해 둔 값이라
     * 목록 조회가 reviews 를 다시 타지 않는다.
     *
     * <p>리뷰가 한 건도 없으면 컬럼 기본값 그대로 {@code 0.00} 이다. "별점 0점"과 "아직 리뷰 없음"은
     * 다른 뜻이라 화면은 {@link #hasRating()} 으로 갈라 그린다.
     */
    private BigDecimal averageRating;

    /**
     * 지도에 찍을 위도. {@code place_addresses} 에 주소 행이 없거나 아직 지오코딩되지 않았으면 {@code null} 이다. (#277)
     *
     * <p>null 인 장소는 지도에서 빠지고 목록에는 그대로 남는다. 화면이 이 값을 필수로 다루면
     * 좌표 하나 없는 장소 때문에 목록 전체가 비게 된다.
     */
    private BigDecimal latitude;

    /**
     * 지도에 찍을 경도. 위도와 짝이며 한쪽만 채워진 행은 좌표가 없는 것으로 본다.
     */
    private BigDecimal longitude;

    /**
     * 별점을 그릴지. 리뷰가 없어 {@code 0.00} 인 장소는 별점 자리를 통째로 비운다.
     *
     * <p>화면에서 {@code averageRating > 0} 을 직접 쓰면 {@code null}(MyBatis 를 거치지 않고
     * 만든 DTO)에서 터진다. 판단을 여기 한 곳에 둔다.
     */
    public boolean hasRating() {
        return averageRating != null && averageRating.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean availableToday;

    /**
     * 카드 대표 사진. {@code place_photos} 에서 {@code sort_order} 가 가장 앞선 한 장이다.
     * 아직 사진을 안 올린 장소는 {@code null} 이라 화면이 고정 아이콘으로 대체한다.
     */
    private String thumbnailUrl;
}
