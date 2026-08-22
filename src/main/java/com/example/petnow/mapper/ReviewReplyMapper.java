package com.example.petnow.mapper;

import com.example.petnow.entity.ReviewReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewReplyMapper {

    void insertReply(ReviewReply reply);

    ReviewReply findByReviewId(Long reviewId);

    void updateReply(@Param("id") Long id, @Param("content") String content);

    void deleteById(Long id);

}
