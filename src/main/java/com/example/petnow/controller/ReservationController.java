package com.example.petnow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petnow.dto.request.ReservationRequest;

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
		return "reservationDetail";
	}
}
