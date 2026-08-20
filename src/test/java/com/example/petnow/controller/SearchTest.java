package com.example.petnow.controller;

import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(value = HomeController.class, properties = "app.public-url=https://petnow.example")
class SearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PetService petService;

    @BeforeEach
    void stubEmptyResult() {
        given(placeService.searchPlaces(any(), any())).willReturn(
                PlaceSearchResponse.builder().places(List.of()).build());
    }

    @Test
    @DisplayName("검색어 없이 들어오면 조회하지 않고 입력을 안내한다")
    void initialScreenDoesNotSearch() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/search"))
                .andExpect(content().string(containsString("name=\"keyword\"")))
                .andExpect(content().string(containsString("찾고 싶은 공간을 검색해보세요")));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("공백을 걷은 키워드로 검색하고 결과 카드를 그린다")
    void searchesByKeyword() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of(PlaceListResponse.builder()
                        .id(3L)
                        .name("마당 있는 우리집")
                        .nickname("초코 보호자")
                        .build()))
                .build());

        mockMvc.perform(get("/search").param("keyword", "  마당  "))
                .andExpect(status().isOk())
                .andExpect(view().name("places/search"))
                .andExpect(content().string(containsString("value=\"마당\"")))
                .andExpect(content().string(containsString("마당 있는 우리집")))
                .andExpect(content().string(containsString("초코 보호자")));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getKeyword()).isEqualTo("마당");
    }

    @Test
    @DisplayName("검색 결과가 없으면 오류 대신 다시 검색할 방법을 안내한다")
    void emptyResultShowsGuidance() throws Exception {
        mockMvc.perform(get("/search").param("keyword", "없는 공간"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("없는 공간")))
                .andExpect(content().string(containsString("검색 결과가 없어요")))
                .andExpect(content().string(containsString("다시 찾아보세요")));
    }

    @Test
    @DisplayName("정렬 값을 필터에 실어 보내고 고른 칩을 켠 채로 그린다")
    void sortReachesFilterAndLightsChip() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of(PlaceListResponse.builder().id(3L).name("마당 있는 우리집").build()))
                .build());

        mockMvc.perform(get("/search").param("keyword", "마당").param("sort", "rating"))
                .andExpect(status().isOk())
                // 고른 칩"만" 켜진다. 켜진 표시가 없으면 정렬이 걸렸는지 화면에서 알 수 없다.
                // 링크까지 함께 본다 — is-active 만 보면 엉뚱한 칩이 켜져도 통과한다.
                .andExpect(content().string(containsString(
                        "is-active\" role=\"tab\" href=\"/search?keyword=%EB%A7%88%EB%8B%B9&amp;sort=rating\"")))
                // 기본값이 아닌 정렬은 다음 검색까지 hidden 으로 따라간다.
                .andExpect(content().string(containsString("name=\"sort\" value=\"rating\"")));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo("rating");
    }

    @Test
    @DisplayName("허용 목록 밖의 정렬 값은 최신순으로 떨어뜨려 그린다")
    void unknownSortFallsBackToLatest() throws Exception {
        // 매퍼가 조용히 최신순으로 떨어뜨리는 값이다. 화면도 같은 판정을 해야
        // 아무 칩도 안 켜진 채 "정렬이 안 걸린 것처럼" 보이지 않는다.
        mockMvc.perform(get("/search").param("keyword", "마당").param("sort", "drop table"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("검색 결과가 없어요")));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().normalizedSort()).isEqualTo(PlaceFilterRequest.SORT_LATEST);
    }
}
