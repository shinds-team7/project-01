package com.example.petnow.service;

import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.mapper.BookmarkMapper;
import com.example.petnow.mapper.PlaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BookmarkServiceImplTest {

    private BookmarkMapper bookmarkMapper;
    private PlaceMapper placeMapper;
    private BookmarkServiceImpl bookmarkService;

    @BeforeEach
    void setUp() {
        bookmarkMapper = mock(BookmarkMapper.class);
        placeMapper = mock(PlaceMapper.class);
        bookmarkService = new BookmarkServiceImpl(bookmarkMapper, placeMapper);
    }

    @Test
    @DisplayName("찜하지 않은 장소는 현재 사용자 기준으로 저장한다")
    void addsBookmarkWhenMissing() {
        given(placeMapper.findDetailById(3L)).willReturn(publicPlace());
        given(bookmarkMapper.existsByUserAndPlace(7L, 3L)).willReturn(false);

        boolean bookmarked = bookmarkService.toggle(7L, 3L);

        assertThat(bookmarked).isTrue();
        verify(bookmarkMapper).insert(7L, 3L);
        verify(bookmarkMapper, never()).delete(7L, 3L);
    }

    @Test
    @DisplayName("이미 찜한 장소는 현재 사용자 기준으로 삭제한다")
    void removesBookmarkWhenAlreadyExists() {
        given(placeMapper.findDetailById(3L)).willReturn(publicPlace());
        given(bookmarkMapper.existsByUserAndPlace(7L, 3L)).willReturn(true);

        boolean bookmarked = bookmarkService.toggle(7L, 3L);

        assertThat(bookmarked).isFalse();
        verify(bookmarkMapper).delete(7L, 3L);
        verify(bookmarkMapper, never()).insert(7L, 3L);
    }

    @Test
    @DisplayName("찜 상태 조회는 사용자와 장소 조합으로만 확인한다")
    void checksBookmarkByUserAndPlace() {
        given(bookmarkMapper.existsByUserAndPlace(7L, 3L)).willReturn(true);

        assertThat(bookmarkService.isBookmarked(7L, 3L)).isTrue();

        verify(bookmarkMapper).existsByUserAndPlace(7L, 3L);
    }

    private PlaceDetailResponse publicPlace() {
        PlaceDetailResponse place = new PlaceDetailResponse();
        place.setId(3L);
        place.setStatus(PlaceStatus.PUBLISHED);
        place.setVisible(true);
        return place;
    }
}
