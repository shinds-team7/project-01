package com.example.petnow.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReservationCancelRequest;
import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.service.ReservationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping("/create")
	public String saveReservation(@RequestBody ReservationRequest request, @RequestParam Long userId) {
		reservationService.saveReservation(request, userId);
		return "reservations/success";
	}

	@GetMapping("/detail")
	public String detailReservation(@ModelAttribute ReservationDetailResponse response,
		@RequestParam Long reservationId, @RequestParam Long userId,
		Model model) {
		ReservationDetailResponse reservation = reservationService.detailReservation(response.getReservationId());
		model.addAttribute("reservation", reservation);
		return "reservations/reservationDetail";
	}

	@GetMapping("/list")
	public String getReservationList(@RequestParam Long userId, @RequestParam(required = false) String useStatus,
		Model model) {
		List<ReservationListResponse> responseList = reservationService.getReservationList(userId, useStatus);
		model.addAttribute("reservations", responseList);
		return "reservations/reservationList";
	}

	@PostMapping("/cancel")
	public String cancel(@ModelAttribute ReservationCancelRequest request, HttpSession session) {
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		reservationService.cancelReservation(request.getReservationId(), userId);
		return "redirect:/reservation/list?userId=" + userId;
	}
}
