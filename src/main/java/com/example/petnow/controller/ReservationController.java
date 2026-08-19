package com.example.petnow.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petnow.common.argument.LoginUser;
import com.example.petnow.dto.request.ReservationCancelRequest;
import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReservationErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.PetService;
import com.example.petnow.service.ReservationService;

import jakarta.validation.Valid;

/**
 * 예약 화면. {@code /reservation/**} 는 전부 로그인이 필요하며 그 판단은
 * {@link com.example.petnow.common.config.WebConfig} 의 인터셉터가 한다.
 * 여기서는 세션을 직접 들여다보지 않고 {@link LoginUser} 로 사용자 id 만 받는다.
 */
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
	public String saveReservation(@LoginUser Long userId, @Valid @ModelAttribute ReservationRequest request,
		BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Long placeId = request.getPlaceId();
            if (placeId == null) {
                throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
            }

            // place 만 다시 담으면 되돌아온 폼에서 반려동물 목록이 사라져,
            // 사용자는 petIds 검증 에러를 보면서도 고를 대상이 없는 상태가 된다.
            fillBookingRequestForm(placeId, userId, model);
            // 1단계(유형 선택)로 되돌리면 애써 고른 날짜·시간이 통째로 날아간다.
            // 일정이 그대로 넘어온 경우에는 확인 단계를 다시 그려 그 자리에서 고치게 한다.
            if (request.getReservationType() != null && request.getCheckIn() != null && request.getCheckOut() != null) {
                fillStep(reservationService.resolveConfirm(placeId, request.getReservationType(),
                    request.getCheckIn(), request.getCheckOut()), model);
                model.addAttribute("errorMessage", firstErrorMessage(bindingResult));
            } else {
                model.addAttribute("step", "type");
            }
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
	public String bookingRequest(@LoginUser Long userId, @RequestParam Long placeId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        fillBookingRequestForm(placeId, userId, model);

        if (type == null || type.isBlank()) {
            model.addAttribute("step", "type");
            return "booking-request";
        }

		ReservationStepResponse result;
		if ("SAME_DAY".equals(type)) {
			result = reservationService.resolveHourly(placeId, date, start, end);
		} else if ("OVERNIGHT".equals(type)) {
			result = reservationService.resolvePackage(placeId, startDate, endDate);
		} else {
			throw new BusinessException(ReservationErrorCode.UNSUPPORTED_RESERVATION_TYPE);
        }

        fillStep(result, model);
        model.addAttribute("errorMessage", result.getErrorMessage());

		return "booking-request";
	}

	/** 단계 응답을 화면이 읽는 모델 이름으로 펼친다. */
	private void fillStep(ReservationStepResponse result, Model model) {
		model.addAttribute("reservationType", result.getReservationType());
		model.addAttribute("step", result.getStep());
		model.addAttribute("slots", result.getSlots());
		model.addAttribute("startSlot", result.getStartSlot());
		model.addAttribute("days", result.getDays());
		model.addAttribute("startDay", result.getStartDay());
		model.addAttribute("selectedDate", result.getSelectedDate());
		model.addAttribute("checkIn", result.getCheckIn());
		model.addAttribute("checkOut", result.getCheckOut());
		model.addAttribute("totalPrice", result.getTotalPrice());
	}

	private String firstErrorMessage(BindingResult bindingResult) {
		return bindingResult.getAllErrors().stream()
			.map(error -> error.getDefaultMessage())
			.filter(message -> message != null && !message.isBlank())
			.findFirst()
			.orElse("입력값을 확인해주세요.");
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
	public String detailReservation(@LoginUser Long userId, @RequestParam Long reservationId, Model model) {
		ReservationDetailResponse reservation = reservationService.detailReservation(reservationId, userId);
		model.addAttribute("reservation", reservation);
		return "reservations/reservationDetail";
	}

	@GetMapping("/list")
	public String getReservationList(@LoginUser Long userId, @RequestParam(required = false) String useStatus, Model model) {
		List<ReservationListResponse> responseList = reservationService.getReservationList(userId, useStatus);
		model.addAttribute("reservations", responseList);
		return "reservations/reservationList";
	}

    @GetMapping("/host")
    public String getReservationHostList(
        @LoginUser Long userId,
        @RequestParam(required = false, defaultValue = "booking") String tab,
        @RequestParam(required = false, defaultValue = "ALL") String status,
        Model model) {
        model.addAttribute("tab", tab);
        model.addAttribute("activeTab", tab);

        if ("booking".equals(tab)) {
            ReservationStatus filterStatus = "ALL".equalsIgnoreCase(status)
                ? null
                : ReservationStatus.valueOf(status.toUpperCase());

            List<ReservationListResponse> reservations =
                reservationService.getReservationByHost(userId, filterStatus);

            model.addAttribute("reservations", reservations);
            model.addAttribute("status", status.toUpperCase());
        }
        return "host/dashboard";
    }

	@PostMapping("/cancel")
	public String cancel(@LoginUser Long userId, @Valid @ModelAttribute ReservationCancelRequest request, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "reservations/reservationList";
		}

		reservationService.cancelReservation(request.getReservationId(), userId);
		return "redirect:/reservation/list";
	}

	@PostMapping("/{reservationId}/approve")
	public String approve(@LoginUser Long hostUserId, @PathVariable Long reservationId) {
		reservationService.approveReservation(reservationId, hostUserId);
		return "redirect:/host?tab=booking";
	}

	@PostMapping("/{reservationId}/reject")
	public String reject(@LoginUser Long hostUserId, @PathVariable Long reservationId) {
		reservationService.rejectReservation(reservationId, hostUserId);
		return "redirect:/host?tab=booking";
	}

    @PostMapping("/{reservationId}/edit")
    public String editReservation(@LoginUser Long userId, @PathVariable Long reservationId, @RequestParam(required = false) List<Long> petIds) {
        reservationService.changeReservationPet(reservationId, petIds, userId);
        return "redirect:/reservation/detail?reservationId=" + reservationId;
    }

    @GetMapping("/{reservationId}")
    public String bookingDetailHost(@LoginUser Long hostUserId, @PathVariable Long reservationId, Model model) {
        ReservationDetailResponse reservation = reservationService.detailReservationForHost(reservationId, hostUserId);

        if (reservation == null) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        model.addAttribute("reservation", reservation);
        return "host/booking-detail";
    }

}
