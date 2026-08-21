package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.common.controller.HomeController;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.LoginUser;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 홈의 검색 조건 카드 → GET /nearby 흐름을 화면 쪽에서 확인한다. (#7)
 *
 * <p>가장 중요한 건 <b>어떤 입력이 와도 500 이 나지 않는 것</b>이다. 잘못된 날짜 문자열이나
 * 모순된 조건은 오류 화면이 아니라 안내 문구와 함께 같은 화면으로 돌아와야 한다.
 */
@WebMvcTest(value = HomeController.class, properties = "app.public-url=https://petnow.example")
class NearbyFilterTest {

    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();
    private static final String DAY_AFTER = LocalDate.now().plusDays(2).toString();

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
    @DisplayName("고른 조건이 그대로 검색 조건으로 전달된다")
    void bindsFilterParameters() throws Exception {
        mockMvc.perform(get("/nearby")
                        .param("regions", "성동구", "광진구")
                        .param("startDate", TOMORROW)
                        .param("startTime", "09:00")
                        .param("endTime", "12:00")
                        .param("placeType", "HOUSE")
                        .param("sort", "price"))
                .andExpect(status().isOk())
                .andExpect(view().name("nearby"));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());

        PlaceFilterRequest filter = captor.getValue();
        assertThat(filter.getRegions()).containsExactly("성동구", "광진구");
        assertThat(filter.getStartDate()).isEqualTo(TOMORROW);
        assertThat(filter.getStartTime()).isEqualTo("09:00");
        assertThat(filter.getEndTime()).isEqualTo("12:00");
        assertThat(filter.getSort()).isEqualTo("price");
    }

    @Test
    @DisplayName("여러 날을 고르면서 시간까지 보내면 검색하지 않고 안내 문구를 그린다")
    void rejectsTimeWithMultipleDays() throws Exception {
        mockMvc.perform(get("/nearby")
                        .param("startDate", TOMORROW)
                        .param("endDate", DAY_AFTER)
                        .param("startTime", "09:00")
                        .param("endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("nearby"))
                .andExpect(content().string(containsString("여러 날을 선택하면 시간은 고를 수 없어요")));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("3시간 격자를 벗어난 시간은 폼 에러가 된다")
    void rejectsTimeOffTheSlotGrid() throws Exception {
        mockMvc.perform(get("/nearby")
                        .param("startDate", TOMORROW)
                        .param("startTime", "10:00")
                        .param("endTime", "13:00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3시간 단위")));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("지난 날짜는 검색하지 않는다")
    void rejectsPastDate() throws Exception {
        mockMvc.perform(get("/nearby").param("startDate", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("지난 날짜는 선택할 수 없어요")));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("시작 시간만 보내면 짝을 맞추라고 안내한다")
    void rejectsHalfOpenTimeRange() throws Exception {
        mockMvc.perform(get("/nearby")
                        .param("startDate", TOMORROW)
                        .param("startTime", "09:00"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("함께 선택해 주세요")));
    }

    @Test
    @DisplayName("날짜 문자열이 깨져도 500 이 아니라 안내 문구로 돌아온다")
    void brokenDateIsNotServerError() throws Exception {
        mockMvc.perform(get("/nearby").param("startDate", "어제"))
                .andExpect(status().isOk())
                .andExpect(view().name("nearby"))
                .andExpect(content().string(containsString("시작 날짜 값을 다시 선택해 주세요")));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("없는 장소 유형을 보내도 500 이 아니다")
    void brokenPlaceTypeIsNotServerError() throws Exception {
        mockMvc.perform(get("/nearby").param("placeType", "CASTLE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("장소 유형 값을 다시 선택해 주세요")));
    }

    @Test
    @DisplayName("고른 장소 유형이 필터에 실려 가고 적용된 조건 칩으로 되비친다")
    void placeTypeReachesFilterAndShowsChip() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of())
                .typeLabel("주택")
                .build());

        mockMvc.perform(get("/nearby").param("placeType", "HOUSE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("주택")))
                // typeLabel 이 isFiltered() 에 들어가야 "조건을 걸었는데 0건"으로 안내된다.
                // 빠뜨리면 유형을 골랐는데도 "주변에 공개된 공간이 없어요" 가 나온다.
                .andExpect(content().string(containsString("조건에 맞는 공간이 없어요")))
                .andExpect(content().string(not(containsString("주변에 공개된 공간이 없어요"))));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getPlaceType()).isEqualTo(PlaceType.HOUSE);
    }

    @Test
    @DisplayName("장소 유형 '전체'(빈 값)는 조건을 걸지 않은 것으로 읽는다")
    void blankPlaceTypeMeansNoCondition() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of(PlaceListResponse.builder().id(3L).name("햇살 가득한 마당").build()))
                .build());

        // 홈의 "전체" 라디오가 보내는 값이다. 이게 조건으로 읽히면 결과가 통째로 사라진다.
        mockMvc.perform(get("/nearby").param("placeType", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("햇살 가득한 마당")));

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getPlaceType()).isNull();
    }

    @Test
    @DisplayName("홈 조건 카드에 장소 유형 선택지가 라디오로 나온다")
    void homeCardOffersPlaceTypeRadios() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"type-modal\"")))
                // 라디오여야 한다. 체크박스면 여러 개가 제출돼 마지막 값만 남는다.
                .andExpect(content().string(containsString("type=\"radio\" name=\"placeType\"")))
                .andExpect(content().string(containsString("주택")))
                // 고른 유형을 되돌릴 수 있어야 한다.
                .andExpect(content().string(containsString("<span>전체</span>")));
    }

    @Test
    @DisplayName("비로그인으로 반려견 조건을 보내면 로그인 화면으로 보낸다")
    void petFilterRequiresLogin() throws Exception {
        mockMvc.perform(get("/nearby").param("petIds", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        then(placeService).should(never()).searchPlaces(any(), any());
    }

    @Test
    @DisplayName("결과가 없으면 오류가 아니라 조건을 넓혀 보라고 안내한다")
    void emptyResultShowsGuidance() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of())
                .regionLabel("성동구")
                .dateLabel("8월 20일")
                .build());

        mockMvc.perform(get("/nearby").param("regions", "성동구").param("startDate", TOMORROW))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("조건에 맞는 공간이 없어요")))
                .andExpect(content().string(not(containsString("주변에 공개된 공간이 없어요"))))
                // 적용한 조건을 칩으로 되비춘다
                .andExpect(content().string(containsString("성동구")))
                .andExpect(content().string(containsString("8월 20일")));
    }

    @Test
    @DisplayName("조건 없이 들어오면 예전처럼 전체 목록 화면이 그려진다")
    void withoutFilterKeepsPlainList() throws Exception {
        given(placeService.searchPlaces(any(), any())).willReturn(PlaceSearchResponse.builder()
                .places(List.of(PlaceListResponse.builder().id(3L).name("햇살 가득한 마당").build()))
                .build());

        mockMvc.perform(get("/nearby"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("햇살 가득한 마당")))
                .andExpect(content().string(containsString("총 1곳")));
    }

    @Test
    @DisplayName("홈의 검색 조건 카드가 /nearby 로 제출되는 폼이다")
    void homeSearchCardIsAForm() throws Exception {
        given(placeService.getFilterRegions()).willReturn(List.of("성동구", "광진구"));

        String html = mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("<form class=\"search-planner reveal\" method=\"get\" action=\"/nearby\" data-place-filter>");
        // 지역 선택지는 하드코딩이 아니라 서버가 내려준 값이다
        assertThat(html).contains("name=\"regions\" value=\"성동구\"");
        assertThat(html).contains("name=\"regions\" value=\"광진구\"");
        // 3시간 격자만 고를 수 있고, 마지막 칸은 24:00 으로 보인다
        assertThat(html).contains("<option value=\"21:00\">21:00</option>");
        assertThat(html).contains("<option value=\"00:00\">24:00</option>");
        assertThat(html).doesNotContain("value=\"10:00\"");
    }

    @Test
    @DisplayName("지역 정보가 아직 없으면 준비 중이라고 안내한다")
    void emptyRegionsShowsNotice() throws Exception {
        given(placeService.getFilterRegions()).willReturn(List.of());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("지역 정보가 준비 중이에요")));
    }

    @Test
    @DisplayName("로그인하면 내 아이가 선택지로 나온다")
    void loggedInUserSeesOwnPets() throws Exception {
        given(petService.getPetList(1L)).willReturn(List.of(
                PetListResponse.builder().id(9L).name("초코").size(Pet.Size.SMALL).build()));

        mockMvc.perform(get("/home").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"petIds\" value=\"9\"")))
                .andExpect(content().string(containsString("초코 · 소형")));
    }

    @Test
    @DisplayName("비로그인이면 반려견 대신 로그인 안내가 나온다")
    void guestSeesLoginGuide() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그인하면 우리 아이에 맞는 공간만")));

        then(petService).should(never()).getPetList(any());
    }

    @Test
    @DisplayName("'지금 만날 수 있는 이웃 호스트'의 검색창은 하단 탭 검색과 같은 /search 로 보낸다")
    void nearbyHostsSearchGoesToSearchScreen() throws Exception {
        String html = mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("<form class=\"result-search\" method=\"get\" action=\"/search\">");
    }

    @Test
    @DisplayName("유형 칩을 고르면 placeType 조건으로 전달된다")
    void nearbyHostsFilterByPlaceType() throws Exception {
        mockMvc.perform(get("/home").param("placeType", "HOUSE"))
                .andExpect(status().isOk());

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getPlaceType()).isEqualTo(PlaceType.HOUSE);
    }

    @Test
    @DisplayName("잘못된 유형 값이 와도 500 대신 조건 없음으로 조용히 넘어간다")
    void nearbyHostsIgnoresInvalidPlaceType() throws Exception {
        mockMvc.perform(get("/home").param("placeType", "does-not-exist"))
                .andExpect(status().isOk());

        ArgumentCaptor<PlaceFilterRequest> captor = ArgumentCaptor.forClass(PlaceFilterRequest.class);
        then(placeService).should().searchPlaces(any(), captor.capture());
        assertThat(captor.getValue().getPlaceType()).isNull();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        session.setAttribute(SessionConst.LOGIN_USER, LoginUser.builder()
                .id(1L)
                .nickname("윤슬")
                .email("host@petnow.kr")
                .build());
        return session;
    }
}
