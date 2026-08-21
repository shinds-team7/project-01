package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.common.storage.ImageCategory;
import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.entity.ReviewPhoto;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReviewMapper;
import com.example.petnow.mapper.ReviewPhotoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 리뷰 사진 업로드·삭제 정책을 고정한다. (#233)
 */
class ReviewServiceImplPhotoTest {

    private ReviewMapper reviewMapper;
    private PlaceMapper placeMapper;
    private ReviewPhotoMapper reviewPhotoMapper;
    private FileStorage fileStorage;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewMapper = mock(ReviewMapper.class);
        placeMapper = mock(PlaceMapper.class);
        reviewPhotoMapper = mock(ReviewPhotoMapper.class);
        fileStorage = mock(FileStorage.class);
        reviewService = new ReviewServiceImpl(reviewMapper, placeMapper, reviewPhotoMapper, fileStorage);

        given(reviewMapper.countReservationOwnedByMember(anyLong(), anyLong())).willReturn(1);
        given(reviewMapper.findPlaceIdByReservationId(anyLong())).willReturn(9L);
    }

    @Test
    @DisplayName("사진을 올리면 0부터 순서대로 저장한다")
    void savesPhotosInOrder() {
        MockMultipartFile first = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MockMultipartFile second = new MockMultipartFile("images", "b.jpg", "image/jpeg", new byte[]{2});
        given(fileStorage.uploadImage(first, ImageCategory.REVIEW)).willReturn("/uploads/reviews/a.jpg");
        given(fileStorage.uploadImage(second, ImageCategory.REVIEW)).willReturn("/uploads/reviews/b.jpg");

        List<MultipartFile> images = List.of(first, second);
        reviewService.createReview(1L, request(images));

        ArgumentCaptor<ReviewPhoto> captor = ArgumentCaptor.forClass(ReviewPhoto.class);
        verify(reviewPhotoMapper, org.mockito.Mockito.times(2)).insertPhoto(captor.capture());
        List<ReviewPhoto> saved = captor.getAllValues();
        assertThat(saved.get(0).getSortOrder()).isZero();
        assertThat(saved.get(0).getImageUrl()).isEqualTo("/uploads/reviews/a.jpg");
        assertThat(saved.get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("5장을 넘기면 업로드 전에 개수 초과 예외를 던진다")
    void rejectsMoreThanFivePhotos() {
        List<MultipartFile> sixFiles = List.of(
                photo("1"), photo("2"), photo("3"), photo("4"), photo("5"), photo("6"));

        assertThatThrownBy(() -> reviewService.createReview(1L, request(sixFiles)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_COUNT_EXCEEDED);

        verify(fileStorage, never()).uploadImage(any(), any());
        verify(reviewPhotoMapper, never()).insertPhoto(any());
    }

    @Test
    @DisplayName("리뷰를 삭제하면 사진 행과 실제 파일을 함께 정리한다")
    void deleteReviewCleansUpPhotos() {
        given(reviewMapper.countReviewById(11L)).willReturn(1);
        given(reviewMapper.countReviewOwnedByMember(1L, 11L)).willReturn(1);
        given(reviewMapper.findPlaceIdByReviewId(11L)).willReturn(9L);
        ReviewPhoto photo = ReviewPhoto.builder().id(5L).reviewId(11L).imageUrl("/uploads/reviews/a.jpg").sortOrder(0).build();
        given(reviewPhotoMapper.findByReviewId(11L)).willReturn(List.of(photo));

        reviewService.deleteReview(1L, 11L);

        verify(reviewPhotoMapper).deleteByReviewId(11L);
        verify(fileStorage).deleteImage("/uploads/reviews/a.jpg");
    }

    private MockMultipartFile photo(String name) {
        return new MockMultipartFile("images", name + ".jpg", "image/jpeg", new byte[]{1});
    }

    private ReviewCreateRequest request(List<MultipartFile> images) {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setReservationId(3L);
        request.setRating(5);
        request.setContent("좋았어요");
        request.setImages(images);
        return request;
    }
}
