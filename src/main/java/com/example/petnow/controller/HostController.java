package com.example.petnow.controller;

import java.util.List;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.request.ReviewReplyRequest;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReviewSortType;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.HostService;
import com.example.petnow.service.ReservationService;
import com.example.petnow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

    private final HostService hostService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final PlaceMapper placeMapper;

    @GetMapping
    public String dashboard(@LoginUser Long loginUserId,
                            @RequestParam(defaultValue = "places") String tab,
                            @RequestParam(required = false, defaultValue = "ALL") String status,
                            Model model) {

        if (!tab.equals("booking") && !tab.equals("reviews") && !tab.equals("places")) {
            tab = "places";
        }

        model.addAttribute("tab", tab);

        if ("booking".equals(tab)) {
            ReservationStatus filterStatus = "ALL".equalsIgnoreCase(status)
                ? null : ReservationStatus.valueOf(status.toUpperCase());

            List<ReservationListResponse> reservations = reservationService.getReservationByHost(loginUserId, filterStatus);

            model.addAttribute("reservations", reservations);
            model.addAttribute("status", status);

        } else if ("reviews".equals(tab)) {
            // 내 장소 전체에 달린 리뷰를 최신순 한 묶음으로 본다.
            // 장소별로 나눠 보는 건 카드에서 GET /host/places/{placeId}/reviews 로 들어간다.
            model.addAttribute("reviews", reviewService.getReviewsForHost(loginUserId));

        } else {
            model.addAttribute("places", hostService.getPlacesByUserId(loginUserId));
        }

        return "host/dashboard";
    }

    /**
     * 장소 하나의 리뷰 관리 화면. 호스트 홈 리뷰 탭과 내 호스팅 탭의 "리뷰 관리" 가 여기로 온다.
     *
     * <p>보호자용 {@code GET /reviews/place/{placeId}} 와 목록 자체는 같지만, 그쪽은 로그인 없이도
     * 열리는 공개 화면이다. 이 화면은 평균 별점 요약과 작성자 표기가 붙어 있어 장소 주인에게만 연다.
     */
    @GetMapping("/places/{placeId}/reviews")
    public String placeReviews(@LoginUser Long hostUserId,
                               @PathVariable Long placeId,
                               Model model) {
        model.addAttribute("place", findOwnedPlace(hostUserId, placeId));
        model.addAttribute("reviews", reviewService.getReviewsByPlace(placeId, ReviewSortType.LATEST));
        return "host/reviews";
    }

    /**
     * 답글 작성·수정. 리뷰 하나에 답글은 최대 1개라 이미 있으면 그대로 덮어쓴다.
     *
     * <p>바인딩 실패는 {@code error.html} 대신 답글 폼이 있던 화면으로 되돌아가야 해서
     * {@code MvcExceptionHandler} 에 맡기지 않고 여기서 직접 플래시 메시지로 처리한다.
     */
    @PostMapping("/places/{placeId}/reviews/{reviewId}/reply")
    public String saveReply(@LoginUser Long hostUserId,
                            @PathVariable Long placeId,
                            @PathVariable Long reviewId,
                            @Valid @ModelAttribute("replyRequest") ReviewReplyRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("replyError",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/host/places/" + placeId + "/reviews";
        }

        reviewService.saveReply(hostUserId, placeId, reviewId, request);
        return "redirect:/host/places/" + placeId + "/reviews";
    }

    @PostMapping("/places/{placeId}/reviews/{reviewId}/reply/delete")
    public String deleteReply(@LoginUser Long hostUserId,
                              @PathVariable Long placeId,
                              @PathVariable Long reviewId) {
        reviewService.deleteReply(hostUserId, placeId, reviewId);
        return "redirect:/host/places/" + placeId + "/reviews";
    }

    /** 남의 장소 리뷰를 URL 만 바꿔 들여다보지 못하게 막는다. HostAvailabilityController 와 같은 규칙이다. */
    private Place findOwnedPlace(Long hostUserId, Long placeId) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        if (!hostUserId.equals(place.getHostUserId())) {
            throw new BusinessException(PlaceErrorCode.PLACE_ACCESS_DENIED);
        }
        return place;
    }
}
