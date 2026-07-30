package com.example.petnow.mapper;

import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Review;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insertReview(Review review);

    List<ReviewResponse> findReviewsByUser(Long userId);

    List<ReviewResponse> findReviewsByPlace(Long placeId);
}
