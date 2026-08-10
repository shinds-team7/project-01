package com.example.petnow.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

	private static final String RESERVATION_NUMBER_PREFIX = "PN";
	private static final String RESERVATION_NUMBER_DATE_PATTERN = "yyyyMMdd";
	private static final int RANDOM_CODE_LENGTH = 8;

	private Long id;
	private Long userId;
	private Long placeId;
	private String reservationNo;
	private String reservationType;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private ReservationStatus status;
	private String memo;
	private BigDecimal totalPrice;
	private LocalDateTime respondedAt;
	private LocalDateTime canceledAt;
	private String cancelReason;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private List<Pet> pets;

	public static String createReservationNo() {
		String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(RESERVATION_NUMBER_DATE_PATTERN));
		String randomCode = UUID.randomUUID().toString().substring(0, RANDOM_CODE_LENGTH).toUpperCase();

		return String.format("%s-%s-%s", RESERVATION_NUMBER_PREFIX, dateStr, randomCode);
	}
}
