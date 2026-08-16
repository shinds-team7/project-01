package com.example.petnow.controller;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReviewResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.PlaceType;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.AuthService;
import com.example.petnow.service.HostService;
import com.example.petnow.service.PetService;
import com.example.petnow.service.PlaceService;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * 이슈 #183 에서 프로토타입 앱 셸로 옮긴 화면들이 실제로 그렇게 그려지는지 확인한다.
 *
 * <p>셸이 빠지면 스타일 없는 맨 HTML 이 나오므로 {@code /css/app.css} 링크 유무로 잡는다.
 * 목록 화면은 항목을 반드시 1개 이상 스텁한다. 빈 목록만 스텁하면 {@code th:each} 본문이
 * 한 번도 실행되지 않아 카드 안의 표현식이 검증되지 않는다(#182 에서 실제로 놓쳤던 부분).
 */
@WebMvcTest(controllers = {
        PlaceController.class,
        ReservationController.class,
        HostController.class,
        ReviewController.class,
        AuthController.class})
class PrototypeShellRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private ReservationService reservationService;

    /** ReservationController 가 예약 폼용으로 직접 들고 있는 의존성이다. */
    @MockitoBean
    private PlaceMapper placeMapper;

    /** 예약 요청 폼이 고를 반려동물 목록을 여기서 받는다 (#187). */
    @MockitoBean
    private PetService petService;

    @MockitoBean
    private HostService hostService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private AuthService authService;

    // ────────────────────────── 장소 ──────────────────────────

    @Test
    @DisplayName("장소 목록이 앱 셸로 그려지고 카드 본문까지 실제로 렌더된다")
    void placeListRendersCards() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(List.of(placeListItem()));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/list"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("class=\"app-nav\"")))
                .andExpect(content().string(containsString("성수 조용한 단독주택 마당")))
                // 장소 이름에 "주택"이 들어 있어 텍스트만 보면 태그가 사라져도 통과한다. 칩 마크업까지 본다.
                .andExpect(content().string(containsString("class=\"is-type\">주택")))
                .andExpect(content().string(containsString("class=\"is-size\">소형견")))
                .andExpect(content().string(containsString("12,000원")));
    }

    @Test
    @DisplayName("공개된 장소가 없으면 앱 셸의 빈 상태를 그린다")
    void placeListRendersEmptyState() throws Exception {
        given(placeService.getPublishedPlaces()).willReturn(List.of());

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("지금은 공개된 공간이 없어요")));
    }

    @Test
    @DisplayName("장소 상세가 앱 셸로 그려지고 예약 요청 CTA 가 실제 매핑을 가리킨다")
    void placeDetailRendersWithCta() throws Exception {
        given(placeService.getPlaceDetail(1L, null)).willReturn(placeDetail());

        mockMvc.perform(get("/places/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/place-detail"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("수용 조건")))
                .andExpect(content().string(containsString("최대 2마리")))
                .andExpect(content().string(containsString("/reservation/booking-request?placeId=1")))
                .andExpect(content().string(containsString("예약 요청하기")));
    }

    // ────────────────────────── 예약 ──────────────────────────

    @Test
    @DisplayName("예약 요청이 매핑 없는 결제 링크 대신 POST /reservation/create 폼으로 그려진다")
    void bookingRequestRendersRealForm() throws Exception {
        given(placeMapper.findById(1L)).willReturn(placeEntity());

        mockMvc.perform(get("/reservation/booking-request").param("placeId", "1").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-request"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("action=\"/reservation/create\"")))
                .andExpect(content().string(containsString("name=\"checkIn\"")))
                .andExpect(content().string(containsString("name=\"checkOut\"")))
                .andExpect(content().string(containsString("name=\"memo\"")))
                // 이식 전에는 매핑이 없는 /places/{id}/payment 로 보내 404 였다.
                // 셸이나 스크립트에 다른 /payment 가 생겨도 안 깨지게 예전 경로만 좁혀서 본다.
                .andExpect(content().string(not(containsString("/places/1/payment"))));
    }

    @Test
    @DisplayName("등록한 반려동물이 없으면 등록 안내가 대신 나온다")
    void bookingRequestGuidesPetRegistration() throws Exception {
        given(placeMapper.findById(1L)).willReturn(placeEntity());
        given(petService.getPetList(1L)).willReturn(List.of());

        mockMvc.perform(get("/reservation/booking-request").param("placeId", "1").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("맡길 반려동물을 먼저 등록해주세요")));
    }

    @Test
    @DisplayName("예약 요청 화면에 로그인 사용자의 반려동물이 체크박스로 내려온다")
    void bookingRequestRendersPetCheckboxes() throws Exception {
        given(placeMapper.findById(1L)).willReturn(placeEntity());
        given(petService.getPetList(1L)).willReturn(List.of(pet()));

        mockMvc.perform(get("/reservation/booking-request").param("placeId", "1").session(loggedIn()))
                .andExpect(status().isOk())
                // petIds 는 @NotEmpty 다. 이 체크박스가 없으면 무엇을 눌러도 예약이 성사되지 않는다.
                .andExpect(content().string(containsString("name=\"petIds\"")))
                .andExpect(content().string(containsString("value=\"7\"")))
                .andExpect(content().string(containsString("초코")))
                .andExpect(content().string(not(containsString("맡길 반려동물을 먼저 등록해주세요"))));
    }

    @Test
    @DisplayName("비로그인 사용자는 예약 요청 화면에 들어가기 전에 홈으로 보내진다")
    void bookingRequestRedirectsAnonymousUser() throws Exception {
        // 제출 시점에 튕기면 날짜·메모를 다 채운 뒤 입력이 통째로 날아간다.
        mockMvc.perform(get("/reservation/booking-request").param("placeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("검증 실패로 폼이 되돌아와도 반려동물 목록이 남아 있다")
    void bookingRequestKeepsPetsOnValidationFailure() throws Exception {
        given(placeMapper.findById(1L)).willReturn(placeEntity());
        given(petService.getPetList(1L)).willReturn(List.of(pet()));

        // petIds 를 비워 보내 @NotEmpty 를 일부러 터뜨린다.
        mockMvc.perform(post("/reservation/create").session(loggedIn())
                        .param("placeId", "1")
                        .param("checkIn", "2026-08-20T15:00")
                        .param("checkOut", "2026-08-20T19:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-request"))
                // place 만 다시 담던 시절에는 고를 대상이 사라진 폼이 돌아왔다.
                .andExpect(content().string(containsString("name=\"petIds\"")))
                .andExpect(content().string(containsString("초코")));
    }

    // ────────────────────────── 호스트 ──────────────────────────

    @Test
    @DisplayName("게시글 작성이 앱 셸로 그려지고 placeType 라디오가 모델에서 온다")
    void hostCreateRendersWithAppShell() throws Exception {
        mockMvc.perform(get("/places/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("places/create"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("name=\"placeType\"")))
                .andExpect(content().string(containsString("아파트")))
                .andExpect(content().string(containsString("오피스텔")))
                .andExpect(content().string(containsString("게시글 등록")));
    }

    @Test
    @DisplayName("기타 옵션 토글의 체크박스 id 와 라벨 for 가 맞는다")
    void hostCreateOtherOptionToggleIsClickable() throws Exception {
        // id 를 직접 안 적으면 Thymeleaf 가 otherOptionsEnabled1 로 그려 라벨과 어긋나고,
        // 체크박스가 숨겨져 있어 토글을 누를 방법이 아예 없어진다.
        mockMvc.perform(get("/places/new").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"otherOptionsEnabled\"")))
                .andExpect(content().string(containsString("for=\"otherOptionsEnabled\"")))
                .andExpect(content().string(not(containsString("otherOptionsEnabled1"))));
    }

    @Test
    @DisplayName("장소 등록 완료가 앱 셸로 그려진다")
    void hostSuccessRendersWithAppShell() throws Exception {
        mockMvc.perform(get("/places/success"))
                .andExpect(status().isOk())
                .andExpect(view().name("places/success"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("등록 접수")));
    }

    // ────────────────────────── 리뷰 ──────────────────────────

    @Test
    @DisplayName("리뷰 작성이 앱 셸로 그려지고 별점 위젯과 스크립트가 함께 온다")
    void reviewFormRendersWithAppShell() throws Exception {
        given(reservationService.detailReservation(9L, 1L)).willReturn(reservation());

        mockMvc.perform(get("/reviews/new").param("reservationId", "9").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/create"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("data-star-picker")))
                .andExpect(content().string(containsString("/js/app-flow.js")))
                // 상단 요약 카드는 placeName 이 모델에 있을 때만 그려진다
                .andExpect(content().string(containsString("성수 조용한 단독주택 마당")))
                .andExpect(content().string(containsString("2026년 7월 18일 이용")));
    }

    @Test
    @DisplayName("내 리뷰 목록이 카드 본문까지 그려지고 수정·삭제가 보인다")
    void myReviewsRenderCards() throws Exception {
        // 목을 given(...) 인자 안에서 만들면 스텁이 겹쳐 UnfinishedStubbingException 이 난다
        ReviewResponse review = review();
        given(reviewService.getMyReviews(1L)).willReturn(List.of(review));

        mockMvc.perform(get("/reviews/my").session(loggedIn()))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/list"))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("성수 조용한 단독주택 마당")))
                .andExpect(content().string(containsString("마당이 넓어서 좋았어요")))
                .andExpect(content().string(containsString("수정하기")));
    }

    @Test
    @DisplayName("장소별 리뷰 목록에는 남의 리뷰를 고칠 수 있는 수정·삭제가 나오지 않는다")
    void placeReviewsHideOwnerActions() throws Exception {
        ReviewResponse review = review();
        given(reviewService.getReviewsByPlace(1L)).willReturn(List.of(review));

        mockMvc.perform(get("/reviews/place/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reviews/list"))
                .andExpect(content().string(containsString("마당이 넓어서 좋았어요")))
                .andExpect(content().string(not(containsString("수정하기"))))
                .andExpect(content().string(not(containsString("삭제하기"))));
    }

    // ────────────────────────── 인증 ──────────────────────────

    @Test
    @DisplayName("로그인·회원가입도 앱 셸의 app.css 로 그려진다")
    void authScreensRenderWithAppShell() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("auth-card")));

        mockMvc.perform(get("/auth/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("auth-card")));
    }

    // ────────────────────────── 스텁 ──────────────────────────

    private PlaceListResponse placeListItem() {
        return PlaceListResponse.builder()
                .id(1L)
                .nickname("김도윤")
                .name("성수 조용한 단독주택 마당")
                .description("마당이 있는 조용한 단독주택입니다.")
                .placeType(PlaceType.HOUSE)
                .allowsSmallDog(true)
                .providesYard(true)
                .hourlyPrice(new BigDecimal("12000"))
                .build();
    }

    private PlaceDetailResponse placeDetail() {
        PlaceDetailResponse place = new PlaceDetailResponse();
        place.setId(1L);
        place.setHostUserId(2L);
        place.setNickname("김도윤");
        place.setName("성수 조용한 단독주택 마당");
        place.setDescription("마당이 있는 조용한 단독주택입니다.");
        place.setPlaceType(PlaceType.HOUSE);
        place.setAreaSize(new BigDecimal("42"));
        place.setCapacity(2);
        place.setAllowsSmallDog(true);
        place.setProvidesYard(true);
        place.setHourlyPrice(new BigDecimal("12000"));
        place.setNightlyPrice(new BigDecimal("48000"));
        place.setStatus(PlaceStatus.PUBLISHED);
        place.setVisible(true);
        return place;
    }

    private Place placeEntity() {
        return Place.builder()
                .id(1L)
                .hostUserId(2L)
                .hostUserNickname("김도윤")
                .name("성수 조용한 단독주택 마당")
                .placeType(PlaceType.HOUSE)
                .areaSize(new BigDecimal("42"))
                .capacity(2)
                .hourlyPrice(new BigDecimal("12000"))
                .status(PlaceStatus.PUBLISHED)
                .visible(true)
                .build();
    }

    /** 리뷰 작성 폼 상단 요약 카드(장소명 · 이용 날짜)에 쓰이는 예약 상세. */
    private ReservationDetailResponse reservation() {
        ReservationDetailResponse reservation = new ReservationDetailResponse();
        reservation.setPlaceName("성수 조용한 단독주택 마당");
        reservation.setCheckIn(LocalDateTime.of(2026, 7, 18, 15, 0));
        return reservation;
    }

    /** ReviewResponse 는 생성자도 세터도 없어 목으로 만든다. */
    private ReviewResponse review() {
        ReviewResponse review = mock(ReviewResponse.class);
        given(review.getId()).willReturn(11L);
        given(review.getPlaceId()).willReturn(1L);
        given(review.getMemberId()).willReturn(1L);
        given(review.getPlaceName()).willReturn("성수 조용한 단독주택 마당");
        given(review.getRating()).willReturn(5);
        given(review.getContent()).willReturn("마당이 넓어서 좋았어요");
        given(review.getCheckInAt()).willReturn(LocalDate.of(2026, 7, 18));
        return review;
    }

    /** 예약 요청 폼의 반려동물 체크박스에 쓰이는 목록 항목. */
    private PetListResponse pet() {
        return PetListResponse.builder()
                .id(7L)
                .name("초코")
                .size(Pet.Size.SMALL)
                .weight(6.0)
                .build();
    }

    private MockHttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_USER_ID, 1L);
        return session;
    }
}
