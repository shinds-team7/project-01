package com.example.petnow.mapper;

import com.example.petnow.entity.ReviewPhoto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewPhotoMapper {

    void insertPhoto(ReviewPhoto photo);

    List<ReviewPhoto> findByReviewId(Long reviewId);

    int countByReviewId(Long reviewId);

    void deleteById(Long id);

    void deleteByReviewId(Long reviewId);

}
