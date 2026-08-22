package com.example.petnow.service;

import com.example.petnow.common.storage.FileStorage;
import com.example.petnow.common.storage.ImageCategory;
import com.example.petnow.dto.request.ReviewCreateRequest;
import com.example.petnow.dto.request.ReviewReplyRequest;
import com.example.petnow.dto.request.ReviewUpdateRequest;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Review;
import com.example.petnow.entity.ReviewPhoto;
import com.example.petnow.entity.ReviewReply;
import com.example.petnow.entity.ReviewSortType;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReviewErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReviewMapper;
import com.example.petnow.mapper.ReviewPhotoMapper;
import com.example.petnow.mapper.ReviewReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final PlaceMapper placeMapper;
    private final ReviewPhotoMapper reviewPhotoMapper;
    private final ReviewReplyMapper reviewReplyMapper;
    private final FileStorage fileStorage;

    // 리뷰 작성
    @Transactional
    public Long createReview(Long memberId, ReviewCreateRequest request) {
        // 이 예약이 실제로 로그인한 회원 본인의 예약인지 검증
        int count = reviewMapper.countReservationOwnedByMember(request.getReservationId(), memberId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        Review review = Review.builder()
                .reservationId(request.getReservationId())
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        // 이미 리뷰가 작성된 예약인지 검증
        try {
            reviewMapper.insertReview(review);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ReviewErrorCode.REVIEW_DUPLICATE);
        }

        saveReviewPhotos(review.getId(), request.getImages());

        // 장소 테이블의 평균 별점 갱신
        Long placeId = reviewMapper.findPlaceIdByReservationId(request.getReservationId());
        placeMapper.updateAvgRating(placeId);

        return review.getId();
    }

    // 내가 작성한 리뷰 목록 조회
    public List<ReviewResponse> getMyReviews(Long memberId) {
        return reviewMapper.findReviewsByMemberId(memberId);
    }

    // 특정 장소의 리뷰 목록 조회
    public List<ReviewResponse> getReviewsByPlace(Long placeId, ReviewSortType sort) {
        return reviewMapper.findReviewsByPlaceId(placeId, sort);
    }

    // 내가 호스팅하는 장소들에 달린 리뷰 목록 조회 (호스트 홈 리뷰 탭)
    public List<ReviewResponse> getReviewsForHost(Long hostUserId) {
        return reviewMapper.findReviewsByHostUserId(hostUserId);
    }

    // 리뷰 단건 조회 (수정 폼 화면 용)
    public ReviewResponse getReview(Long memberId, Long reviewId) {
        ReviewResponse response = reviewMapper.findResponseById(reviewId)
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

        int count = reviewMapper.countReviewOwnedByMember(memberId, reviewId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }

        return response;
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(Long memberId, Long reviewId, ReviewUpdateRequest request) {
        // 리뷰 존재 여부 확인
        if (reviewMapper.countReviewById(reviewId) == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        // 본인이 작성한 리뷰인지 검증
        int count = reviewMapper.countReviewOwnedByMember(memberId, reviewId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        Long placeId = reviewMapper.findPlaceIdByReviewId(reviewId);

        reviewMapper.updateReview(reviewId, request.getRating(), request.getContent());
        placeMapper.updateAvgRating(placeId);
    }

    // 리뷰 삭제 (soft delete)
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {
        if (reviewMapper.countReviewById(reviewId) == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }

        int count = reviewMapper.countReviewOwnedByMember(memberId, reviewId);
        if (count == 0) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        Long placeId = reviewMapper.findPlaceIdByReviewId(reviewId);

        reviewMapper.deleteReview(reviewId, LocalDateTime.now());
        placeMapper.updateAvgRating(placeId);

        // 소프트 삭제라 review_photos 는 FK CASCADE 로 지워지지 않는다. S3 파일과 행을 직접 정리한다.
        List<ReviewPhoto> photos = reviewPhotoMapper.findByReviewId(reviewId);
        if (!photos.isEmpty()) {
            reviewPhotoMapper.deleteByReviewId(reviewId);
            photos.forEach(photo -> fileStorage.deleteImage(photo.getImageUrl()));
        }

        // 같은 이유로 답글도 남는다. 삭제된 리뷰에 답글만 덩그러니 남을 이유가 없다.
        ReviewReply reply = reviewReplyMapper.findByReviewId(reviewId);
        if (reply != null) {
            reviewReplyMapper.deleteById(reply.getId());
        }
    }

    // 호스트 답글 작성·수정
    @Transactional
    public void saveReply(Long hostUserId, Long placeId, Long reviewId, ReviewReplyRequest request) {
        validateReplyOwnership(hostUserId, placeId, reviewId);

        ReviewReply existing = reviewReplyMapper.findByReviewId(reviewId);
        if (existing != null) {
            reviewReplyMapper.updateReply(existing.getId(), request.getContent());
            return;
        }

        reviewReplyMapper.insertReply(ReviewReply.builder()
                .reviewId(reviewId)
                .hostUserId(hostUserId)
                .content(request.getContent())
                .build());
    }

    // 호스트 답글 삭제
    @Transactional
    public void deleteReply(Long hostUserId, Long placeId, Long reviewId) {
        validateReplyOwnership(hostUserId, placeId, reviewId);

        ReviewReply existing = reviewReplyMapper.findByReviewId(reviewId);
        if (existing == null) {
            throw new BusinessException(ReviewErrorCode.REVIEW_REPLY_NOT_FOUND);
        }
        reviewReplyMapper.deleteById(existing.getId());
    }

    /**
     * 답글은 그 리뷰가 달린 장소의 호스트만 남길 수 있다.
     *
     * <p>{@code placeId} 를 경로에서 받는 이유는 호스트 화면이 이미 장소 단위(
     * {@code /host/places/{placeId}/reviews})로 열리기 때문이다. 그 값을 그대로 믿지 않고
     * 실제 리뷰가 그 장소 것이 맞는지까지 다시 확인해야, URL 의 placeId 만 바꿔 남의 리뷰에
     * 답글을 다는 것을 막을 수 있다.
     */
    private void validateReplyOwnership(Long hostUserId, Long placeId, Long reviewId) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        if (!hostUserId.equals(place.getHostUserId())) {
            throw new BusinessException(PlaceErrorCode.PLACE_ACCESS_DENIED);
        }

        Long reviewPlaceId = reviewMapper.findPlaceIdByReviewId(reviewId);
        if (reviewPlaceId == null || !reviewPlaceId.equals(placeId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
        }
    }

    /**
     * 파일마다 업로드하고 {@code sortOrder} 를 0부터 순서대로 넣는다. (#233)
     *
     * <p>개수 제한은 {@code countByReviewId} 로 직접 확인한다. 리뷰 작성 시점엔 항상 0장에서
     * 시작하지만, 이 메서드가 나중에 수정(추가) 흐름에서도 그대로 재사용될 수 있도록
     * 기존 장수를 함께 센다.
     */
    private void saveReviewPhotos(Long reviewId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        List<MultipartFile> uploadTargets = images.stream()
                .filter(Objects::nonNull)
                .filter(image -> !image.isEmpty())
                .toList();
        if (uploadTargets.isEmpty()) {
            return;
        }

        int existingCount = reviewPhotoMapper.countByReviewId(reviewId);
        if (existingCount + uploadTargets.size() > ImageCategory.REVIEW.getMaxCount()) {
            throw new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED);
        }

        for (int i = 0; i < uploadTargets.size(); i++) {
            String imageUrl = fileStorage.uploadImage(uploadTargets.get(i), ImageCategory.REVIEW);
            reviewPhotoMapper.insertPhoto(ReviewPhoto.builder()
                    .reviewId(reviewId)
                    .imageUrl(imageUrl)
                    .sortOrder(existingCount + i)
                    .build());
        }
    }
}
