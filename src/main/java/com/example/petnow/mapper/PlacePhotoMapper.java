package com.example.petnow.mapper;

import com.example.petnow.entity.PlacePhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlacePhotoMapper {

    int insertPhoto(PlacePhoto photo);

    List<PlacePhoto> findByPlaceId(Long placeId);

    PlacePhoto findByIdAndPlaceId(@Param("photoId") Long photoId,
                                  @Param("placeId") Long placeId);

    int countByPlaceId(Long placeId);

    int findNextSortOrder(Long placeId);

    int deleteById(Long photoId);

    int deleteByPlaceId(Long placeId);
}
