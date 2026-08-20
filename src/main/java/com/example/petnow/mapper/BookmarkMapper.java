package com.example.petnow.mapper;

import com.example.petnow.dto.response.PlaceListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    boolean existsByUserAndPlace(@Param("userId") Long userId,
                                 @Param("placeId") Long placeId);

    int insert(@Param("userId") Long userId,
               @Param("placeId") Long placeId);

    int delete(@Param("userId") Long userId,
               @Param("placeId") Long placeId);

    List<PlaceListResponse> findPlacesByUserId(@Param("userId") Long userId);

    /**
     * 이 사용자가 찜한 장소의 id 만 한 번에 읽는다.
     *
     * <p>목록 카드마다 하트를 채우려고 {@link #existsByUserAndPlace} 를 부르면 장소 수만큼 쿼리가 나간다(N+1).
     * id 만 통째로 받아 메모리에서 맞추면 목록 크기와 상관없이 한 번이다.
     */
    List<Long> findPlaceIdsByUserId(@Param("userId") Long userId);
}
