package com.example.petnow.mapper;

import com.example.petnow.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper {

    void insertReview(Review review);

}
