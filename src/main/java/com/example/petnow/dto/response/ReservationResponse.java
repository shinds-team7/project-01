package com.example.petnow.dto.response;

import java.time.LocalDateTime;

import com.example.petnow.entity.ReservationStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponse {

	// 결제정보 추가해야함
	private String nickname;
	private PlaceDTO place;
	private PetInfoDTO pet;
	private String reservationNo;
	private ReservationStatus status;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;


	@Getter
	@Builder
	public static class PetInfoDTO {
		private Long petId;
		private String petName;
		private Integer weight;
	}

	@Getter
	@Builder
	public static class PlaceDTO {
		private Long placeId;
		private String placeTitle;
		private String address;
		private Integer price;
	}
}
