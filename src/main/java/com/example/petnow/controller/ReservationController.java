package com.example.petnow.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReservationCancelRequest;
import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.PetService;
import com.example.petnow.service.ReservationService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
	private final ReservationService reservationService;
	private final PlaceMapper placeMapper;
	private final PetService petService;

    public ReservationController(ReservationService reservationService, PlaceMapper placeMapper, PetService petService) {
		this.reservationService = reservationService;
		this.placeMapper = placeMapper;
		this.petService = petService;
    }

	@PostMapping("/create")
	public String saveReservation(@Valid @ModelAttribute ReservationRequest request,
		BindingResult bindingResult, HttpSession session, Model model, RedirectAttributes redirectAttributes) {

		Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}

        if (bindingResult.hasErrors()) {
            Long placeId = request.getPlaceId();
            if (placeId == null) {
                throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
            }

            // place 만 다시 담으면 되돌아온 폼에서 반려동물 목록이 사라져,
            // 사용자는 petIds 검증 에러를 보면서도 고를 대상이 없는 상태가 된다.
            fillBookingRequestForm(placeId, userId, model);
            model.addAttribute("step", "confirm");
            model.addAttribute("reservationType", request.getReservationType() != null ? request.getReservationType().name() : null);
            model.addAttribute("checkIn", request.getCheckIn());
            model.addAttribute("checkOut", request.getCheckOut());

            return "booking-request";
        }

		String reservationNo = reservationService.saveReservation(request, userId);
		redirectAttributes.addFlashAttribute("reservationNo", reservationNo);
		return "redirect:/reservation/success";
	}

	@GetMapping("/success")
	public String success(Model model) {
		if (!model.containsAttribute("reservationNo")) {
			return "redirect:/reservation/list";
		}
		return "reservations/success";
	}

	@GetMapping("/booking-request")
	public String bookingRequest(@RequestParam Long placeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}

        fillBookingRequestForm(placeId, userId, model);

        if (type == null || type.isBlank()) {
            model.addAttribute("step", "type");
            return "booking-request";
        }

        ReservationStepResponse result;
        if ("SAME_DAY".equals(type)) {
            result = reservationService.resolveHourly(placeId, date, start, end);
        } else {
            result = reservationService.resolvePackage(placeId, startDate, endDate);
        }

        model.addAttribute("reservationType", result.getReservationType());
        model.addAttribute("step", result.getStep());
        model.addAttribute("slots", result.getSlots());
        model.addAttribute("startSlot", result.getStartSlot());
        model.addAttribute("days", result.getDays());
        model.addAttribute("startDay", result.getStartDay());
        model.addAttribute("selectedDate", result.getSelectedDate());
        model.addAttribute("errorMessage", result.getErrorMessage());
        model.addAttribute("checkIn", result.getCheckIn());
        model.addAttribute("checkOut", result.getCheckOut());

		return "booking-request";
	}

	/**
	 * 예약 요청 폼이 필요로 하는 모델을 채운다.
	 *
	 * <p>{@code ReservationRequest.petIds} 가 {@code @NotEmpty} 라 화면에 반려동물 목록이
	 * 없으면 무엇을 눌러도 검증에서 막힌다. 최초 진입과 검증 실패 복귀 두 경로가 같은 화면을
	 * 그리므로 담는 값도 한곳에서 맞춘다.
	 */
	private void fillBookingRequestForm(Long placeId, Long userId, Model model) {
		Place place = placeMapper.findById(placeId);
		if (place == null) {
			throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
		}

		model.addAttribute("place", place);
		model.addAttribute("pets", petService.getPetList(userId));

	}

	@GetMapping("/detail")
	public String detailReservation(@RequestParam Long reservationId, HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		ReservationDetailResponse reservation = reservationService.detailReservation(reservationId, userId);
		model.addAttribute("reservation", reservation);
		return "reservations/reservationDetail";
	}

	@GetMapping("/list")
	public String getReservationList(@RequestParam(required = false) String useStatus, HttpSession session, Model model) {
		Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		List<ReservationListResponse> responseList = reservationService.getReservationList(userId, useStatus);
		model.addAttribute("reservations", responseList);
		return "reservations/reservationList";
	}

	@PostMapping("/cancel")
	public String cancel(@Valid @ModelAttribute ReservationCancelRequest request, HttpSession session, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "reservations/reservationList";
		}

		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		reservationService.cancelReservation(request.getReservationId(), userId);
		return "redirect:/reservation/list";
	}

	@PostMapping("/{reservationId}/approve")
	public String approve(@PathVariable Long reservationId, HttpSession session) {
		Long hostUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (hostUserId == null) {
			return "redirect:/";
		}

		reservationService.approveReservation(reservationId, hostUserId);
		return "redirect:/host?tab=booking";
	}

	@PostMapping("/{reservationId}/reject")
	public String reject(@PathVariable Long reservationId, HttpSession session) {
		Long hostUserId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (hostUserId == null) {
			return "redirect:/";
		}

		reservationService.rejectReservation(reservationId, hostUserId);
		return "redirect:/host?tab=booking";
	}
}
