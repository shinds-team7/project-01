package com.example.petnow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petnow.dto.request.ReservationRequest;

import com.example.petnow.service.ReservationService;

@Controller
public class ReservationController {
	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping("/api/reservation/create")
	public String save(@RequestBody ReservationRequest request, @RequestParam Long userId) {
		reservationService.save(request, userId);
		return "reservationDetail";
	}
}
