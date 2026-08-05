package com.example.petnow.mapper;

import com.example.petnow.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlaceMapper {
    int insert(Place place);

    Place findById(@Param("id") Long id);
}