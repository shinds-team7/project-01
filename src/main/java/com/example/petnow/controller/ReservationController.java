package com.example.petnow.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petnow.common.constant.SessionConst;
import com.example.petnow.dto.request.ReservationCancelRequest;
import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.service.ReservationService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
	private final ReservationService reservationService;
	private final PlaceMapper placeMapper;

	public ReservationController(ReservationService reservationService, PlaceMapper placeMapper) {
		this.reservationService = reservationService;
		this.placeMapper = placeMapper;
	}

	@PostMapping("/create")
	public String saveReservation(@Valid @ModelAttribute ReservationRequest request,
		BindingResult bindingResult, HttpSession session, Model model) {
		if (bindingResult.hasErrors()) {
			Place place = placeMapper.findById(request.getPlaceId());
			model.addAttribute("place", place);
			return "booking-request";
		}
		Long userId = (Long) session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		reservationService.saveReservation(request, userId);
		return "redirect:/reservation/list?userId=" + userId;
	}

	@GetMapping("/booking-request")
	public String bookingRequest(@RequestParam Long placeId, Model model) {
		Place place = placeMapper.findById(placeId);
		if (place == null) {
			throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
		}
		model.addAttribute("place", place);
		return "booking-request";
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
	public String cancel(@ModelAttribute ReservationCancelRequest request, HttpSession session) {
		Long userId = (Long)session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			return "redirect:/";
		}
		reservationService.cancelReservation(request.getReservationId(), userId);
		return "redirect:/reservation/list?userId=" + userId;
	}
}
