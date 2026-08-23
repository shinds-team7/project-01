package com.example.petnow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationUseStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetailResponse {

	// 결제정보, 주소, 장소사진 추가해야함
	private Long reservationId;
	private String reservationNo;
	private ReservationStatus status;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String userName;
	private String placeName;
    private String placeAddress;
    private String placeImageUrl;
	private List<PetDetail> pets;
    private ReservationUseStatus useStatus;

	@Getter
	@Setter
	public static class PetDetail {
		private Long petId;
		private String petName;
		private Double weight;
		private String size;
        private String petImageUrl;
	}
}
