package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.dto.request.ReviewReplyRequest;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.ReviewReply;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReviewErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReviewMapper;
import com.example.petnow.mapper.ReviewPhotoMapper;
import com.example.petnow.mapper.ReviewReplyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 호스트 답글의 소유권 검증과 작성·수정·삭제 정책을 고정한다. (#328)
 */
class ReviewServiceImplReplyTest {

    private ReviewMapper reviewMapper;
    private PlaceMapper placeMapper;
    private ReviewReplyMapper reviewReplyMapper;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewMapper = mock(ReviewMapper.class);
        placeMapper = mock(PlaceMapper.class);
        reviewReplyMapper = mock(ReviewReplyMapper.class);
        reviewService = new ReviewServiceImpl(
                reviewMapper, placeMapper, mock(ReviewPhotoMapper.class), reviewReplyMapper, mock(FileStorage.class));

        given(placeMapper.findById(3L)).willReturn(Place.builder().id(3L).hostUserId(1L).build());
        given(reviewMapper.findPlaceIdByReviewId(11L)).willReturn(3L);
    }

    @Test
    @DisplayName("답글이 없으면 새로 등록한다")
    void insertsReplyWhenNoneExists() {
        given(reviewReplyMapper.findByReviewId(11L)).willReturn(null);

        reviewService.saveReply(1L, 3L, 11L, request("잘 지내다 가셨어요, 감사합니다!"));

        verify(reviewReplyMapper).insertReply(org.mockito.ArgumentMatchers.argThat(reply ->
                reply.getReviewId().equals(11L)
                        && reply.getHostUserId().equals(1L)
                        && reply.getContent().equals("잘 지내다 가셨어요, 감사합니다!")));
        verify(reviewReplyMapper, never()).updateReply(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("답글이 이미 있으면 새로 만들지 않고 내용만 덮어쓴다")
    void updatesExistingReplyInPlace() {
        given(reviewReplyMapper.findByReviewId(11L)).willReturn(
                ReviewReply.builder().id(9L).reviewId(11L).hostUserId(1L).content("old").build());

        reviewService.saveReply(1L, 3L, 11L, request("new content"));

        verify(reviewReplyMapper).updateReply(9L, "new content");
        verify(reviewReplyMapper, never()).insertReply(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("남의 장소 리뷰에는 답글을 달 수 없다")
    void rejectsReplyToOtherHostsPlace() {
        assertThatThrownBy(() -> reviewService.saveReply(99L, 3L, 11L, request("아무거나")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(PlaceErrorCode.PLACE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("URL 의 placeId 와 리뷰가 실제로 속한 장소가 다르면 막는다")
    void rejectsWhenReviewDoesNotBelongToPlace() {
        given(reviewMapper.findPlaceIdByReviewId(11L)).willReturn(4L);

        assertThatThrownBy(() -> reviewService.saveReply(1L, 3L, 11L, request("아무거나")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("답글을 삭제하면 행이 실제로(하드) 삭제된다")
    void deletesReplyRow() {
        given(reviewReplyMapper.findByReviewId(11L)).willReturn(
                ReviewReply.builder().id(9L).reviewId(11L).hostUserId(1L).content("old").build());

        reviewService.deleteReply(1L, 3L, 11L);

        verify(reviewReplyMapper).deleteById(9L);
    }

    @Test
    @DisplayName("삭제할 답글이 없으면 예외를 던진다")
    void rejectsDeleteWhenReplyMissing() {
        given(reviewReplyMapper.findByReviewId(11L)).willReturn(null);

        assertThatThrownBy(() -> reviewService.deleteReply(1L, 3L, 11L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ReviewErrorCode.REVIEW_REPLY_NOT_FOUND);
    }

    private ReviewReplyRequest request(String content) {
        ReviewReplyRequest request = new ReviewReplyRequest();
        request.setContent(content);
        return request;
    }
}
