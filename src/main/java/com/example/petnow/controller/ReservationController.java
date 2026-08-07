package com.example.petnow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.service.ReservationService;

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
	public String detailReservation(@ModelAttribute ReservationDetailResponse response, @RequestParam Long reservationId, @RequestParam Long userId,
		Model model) {
		ReservationDetailResponse reservation = reservationService.detailReservation(response.getReservationId());
		model.addAttribute("reservation", reservation);
		return "reservations/reservationDetail";
	}
}
