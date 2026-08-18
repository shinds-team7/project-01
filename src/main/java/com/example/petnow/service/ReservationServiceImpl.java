package com.example.petnow.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petnow.dto.request.ReservationRequest;
import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.dto.response.ReservationDetailResponse;
import com.example.petnow.dto.response.ReservationListResponse;
import com.example.petnow.dto.response.ReservationStepResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.Reservation;
import com.example.petnow.entity.ReservationStatus;
import com.example.petnow.entity.ReservationType;
import com.example.petnow.entity.ReservationUseStatus;
import com.example.petnow.entity.SlotStatus;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReservationErrorCode;
import com.example.petnow.mapper.PetMapper;

import com.example.petnow.mapper.PlaceAvailabilityMapper;
import com.example.petnow.mapper.PlaceMapper;
import com.example.petnow.mapper.ReservationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
	private final ReservationMapper reservationMapper;
	private final PlaceMapper placeMapper;
	private final PetMapper petMapper;
    private final PlaceAvailabilityMapper placeAvailabilityMapper;

	private static final int SLOT_HOURS = 3;
	private static final long MIN_RESERVATION_MINUTES = SLOT_HOURS * 60;

	@Override
	@Transactional
	public String saveReservation(ReservationRequest request, Long userId) {
		Place place = placeMapper.findById(request.getPlaceId());
		if (place == null) {
			throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
		}

		ReservationType type = request.getReservationType();
		if (type == ReservationType.SAME_DAY && !place.isSupportsHourly()) {
			throw new BusinessException(ReservationErrorCode.UNSUPPORTED_RESERVATION_TYPE);
		}
		if (type == ReservationType.OVERNIGHT && !place.isSupportsPackage()) {
			throw new BusinessException(ReservationErrorCode.UNSUPPORTED_RESERVATION_TYPE);
		}

		if (request.getCheckOut().isBefore(request.getCheckIn())
			|| request.getCheckOut().isEqual(request.getCheckIn())) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
		}

		if (Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes() < MIN_RESERVATION_MINUTES) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_TOO_SHORT);
		}

		LocalDate checkInDate = request.getCheckIn().toLocalDate();
		LocalDate checkOutDate = request.getCheckOut().toLocalDate();
		if (request.getReservationType() == ReservationType.SAME_DAY && !((checkInDate.isEqual(checkOutDate)) || (checkOutDate.isEqual(checkInDate.plusDays(1)) && request.getCheckOut().toLocalTime().equals(LocalTime.MIDNIGHT)))) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
		}
		if (request.getReservationType() == ReservationType.OVERNIGHT) {
			LocalTime expectedCheckIn = place.getPackageCheckInTime();
			if (expectedCheckIn == null) {
				expectedCheckIn = PlaceAvailabilityServiceImpl.DEFAULT_PACKAGE_CHECK_IN_TIME;
			}
			LocalTime expectedCheckOut = place.getPackageCheckOutTime();
			if (expectedCheckOut == null) {
				expectedCheckOut = PlaceAvailabilityServiceImpl.DEFAULT_PACKAGE_CHECK_OUT_TIME;
			}

			boolean timeMatches = request.getCheckIn().toLocalTime().equals(expectedCheckIn) &&
				request.getCheckOut().toLocalTime().equals(expectedCheckOut);
			long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

			if (!timeMatches || nights<1) {
				throw new BusinessException(ReservationErrorCode.INVALID_PACKAGE_TIME);
			}
		}

		List<PetListResponse> myPets = petMapper.getPetList(userId);
		Set<Long> myPetIds = myPets.stream()
			.map(PetListResponse::getId)
			.collect(Collectors.toSet());
		if (!myPetIds.containsAll(request.getPetIds())) {
			throw new BusinessException(ReservationErrorCode.PET_NOT_FOUND);
		}

        long totalHours = Duration.between(request.getCheckIn(), request.getCheckOut()).toHours();
        if (totalHours % SLOT_HOURS != 0) {
            throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_PERIOD);
        }

        long slotAmount = totalHours / SLOT_HOURS;
        List<PlaceSlotResponse> actualSlots = placeAvailabilityMapper.findSlotsByPlaceAndPeriod(request.getPlaceId(), request.getCheckIn(), request.getCheckOut());

        if (actualSlots.size() != slotAmount) {
            throw new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE);
        }

        for (int i=0; i<actualSlots.size(); i++) {
            if (!"OPEN".equals(actualSlots.get(i).getStatus())) {
                throw new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE);
            }
        }

		String reservationNo = Reservation.createReservationNo();
		BigDecimal totalPrice = calculateTotalPrice(place, request.getReservationType(), request.getCheckIn(), request.getCheckOut());

		Reservation reservation = Reservation.builder()
			.placeId(request.getPlaceId())
			.userId(userId)
			.memo(request.getMemo())
			.totalPrice(totalPrice)
			.reservationType(request.getReservationType())
			.checkIn(request.getCheckIn())
			.checkOut(request.getCheckOut())
			.status(ReservationStatus.PENDING)
			.reservationNo(reservationNo)
			.build();

		reservationMapper.save(reservation);

		List<Long> slotIds = actualSlots.stream()
                .map(PlaceSlotResponse::getSlotId)
                .collect(Collectors.toList());

        try {
            reservationMapper.insertReservationSlots(reservation.getId(), slotIds);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ReservationErrorCode.SLOT_ALREADY_TAKEN);
        }

        for (Long slotId : slotIds) {
            placeAvailabilityMapper.updateSlotStatus(request.getPlaceId(), slotId, SlotStatus.RESERVED);
        }

		reservationMapper.saveReservationPets(reservation.getId(), request.getPetIds());
		return reservationNo;
	}

	@Override
	public List<ReservationListResponse> getReservationList(Long userId, String useStatus) {
		ReservationUseStatus status = parseUseStatus(useStatus);

		boolean beforeUse = (status == ReservationUseStatus.BEFORE_USE);
		boolean inUse = (status == ReservationUseStatus.IN_USE);
		boolean afterUse = (status == ReservationUseStatus.AFTER_USE);

		return reservationMapper.viewReservationList(userId, beforeUse, inUse, afterUse);
	}

	@Override
	public ReservationDetailResponse detailReservation(Long reservationId, Long userId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null || !reservation.getUserId().equals(userId)) {
			throw new BusinessException(AuthErrorCode.FORBIDDEN);
		}

		return reservationMapper.detailReservation(reservationId);
	}

	@Override
	@Transactional
	public void cancelReservation(Long reservationId, Long userId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		if (!reservation.getUserId().equals(userId)) {
			throw new BusinessException(AuthErrorCode.FORBIDDEN);
		}

        List<Long> slotIds = reservationMapper.findSlotIdsByReservationId(reservationId);
        placeAvailabilityMapper.updateSlotsToOpen(slotIds);
        reservationMapper.deleteReservationSlotsByReservationId(reservationId);

		int updatedRows = reservationMapper.cancelReservation(reservationId);
		if (updatedRows == 0) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}
	}

	@Override
	@Transactional
	public void approveReservation(Long reservationId, Long hostUserId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		PlaceDetailResponse place = placeMapper.findDetailById(reservation.getPlaceId());
		if (place == null || !place.getHostUserId().equals(hostUserId)) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_ACCESS_DENIED);
		}

		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
		}

		int result = reservationMapper.approveReservation(reservationId);
		if (result != 1) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}
	}

	@Override
	@Transactional
	public void rejectReservation(Long reservationId, Long hostUserId) {
		Reservation reservation = reservationMapper.findById(reservationId);
		if (reservation == null) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_NOT_FOUND);
		}

		PlaceDetailResponse place = placeMapper.findDetailById(reservation.getPlaceId());
		if (place == null || !place.getHostUserId().equals(hostUserId)) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_ACCESS_DENIED);
		}

		if (reservation.getStatus() != ReservationStatus.PENDING) {
			throw new BusinessException(ReservationErrorCode.INVALID_RESERVATION_STATUS);
		}

        List<Long> slotIds = reservationMapper.findSlotIdsByReservationId(reservationId);
        placeAvailabilityMapper.updateSlotsToOpen(slotIds);
        reservationMapper.deleteReservationSlotsByReservationId(reservationId);

		int result = reservationMapper.rejectReservation(reservationId);
		if (result != 1) {
			throw new BusinessException(ReservationErrorCode.RESERVATION_UPDATE_FAILED);
		}
	}

	@Override
	public List<ReservationListResponse> getReservationByHost(Long loginUserId, ReservationStatus status) {
		return reservationMapper.viewReservationListByHost(loginUserId, status);
	}

	private ReservationUseStatus parseUseStatus(String useStatus) {
		if (useStatus == null || useStatus.trim().isEmpty()) {
			return null;
		}
		try {
			return ReservationUseStatus.valueOf(useStatus.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

    @Override
    public ReservationStepResponse resolveHourly(Long placeId, String date, Long start, Long end) {
        if (date == null || date.trim().isEmpty()) {
            return ReservationStepResponse.builder()
                .reservationType(ReservationType.SAME_DAY)
                .step("hourly-date")
                .build();
        }
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime fromAt = localDate.atStartOfDay();
        LocalDateTime toAt = localDate.plusDays(1).atStartOfDay();
        List<PlaceSlotResponse> slots = placeAvailabilityMapper.findSlotsByPlaceAndPeriod(placeId, fromAt, toAt);

        if (start == null) {
            return ReservationStepResponse.builder()
                .reservationType(ReservationType.SAME_DAY)
                .step("hourly-slot")
                .slots(slots)
                .selectedDate(date)
                .build();
        }

        PlaceSlotResponse startSlot = findSlot(slots, start);

        if (end == null) {
            return ReservationStepResponse.builder()
                .reservationType(ReservationType.SAME_DAY)
                .step("hourly-slot")
                .slots(slots)
                .startSlot(startSlot)
                .selectedDate(date)
                .build();
        }

        PlaceSlotResponse endSlot = findSlot(slots, end);
        return validateAndBuildRange(slots, startSlot, endSlot, date);
    }

    @Override
    public ReservationStepResponse resolvePackage(Long placeId, String startDate, String endDate) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        List<PackageDayResponse> days = buildPackageDays(placeId);

        if (startDate == null || startDate.trim().isEmpty()) {
            return ReservationStepResponse.builder()
                .step("package-day")
                .days(days)
                .build();
        }

        PackageDayResponse startDay = findDay(days, startDate);

        if (endDate == null || endDate.trim().isEmpty()) {
            return ReservationStepResponse.builder()
                .step("package-day")
                .days(days)
                .startDay(startDay)
                .build();
        }

        PackageDayResponse endDay = findDay(days, endDate);
        return validateAndBuildDayRange(place, placeId, days, startDay, endDay);
    }

    private PlaceSlotResponse findSlot(List<PlaceSlotResponse> slots, Long slotId) {
        return slots.stream()
            .filter(s -> s.getSlotId().equals(slotId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE));
    }

    private ReservationStepResponse validateAndBuildRange(List<PlaceSlotResponse> slots, PlaceSlotResponse startSlot, PlaceSlotResponse endSlot, String date) {
        PlaceSlotResponse from;
        PlaceSlotResponse to;

        if (startSlot.getStartAt().isBefore(endSlot.getStartAt())) {
            from = startSlot;
            to = endSlot;
        } else {
            from = endSlot;
            to = startSlot;
        }

        List<PlaceSlotResponse> slotsInRange = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            PlaceSlotResponse slot = slots.get(i);

            boolean afterOrEqualFrom = !slot.getStartAt().isBefore(from.getStartAt());
            boolean beforeOrEqualTo = !slot.getStartAt().isAfter(to.getStartAt());

            if (afterOrEqualFrom && beforeOrEqualTo) {
                slotsInRange.add(slot);
            }
        }

        boolean blockedSlot = false;
        for (int i = 0; i < slotsInRange.size(); i++) {
            PlaceSlotResponse slot = slotsInRange.get(i);

            if (!"OPEN".equals(slot.getStatus())) {
                blockedSlot = true;
                break;
            }
        }

        if (blockedSlot) {
            return ReservationStepResponse.builder()
                .step("hourly-slot")
                .reservationType(ReservationType.SAME_DAY)
                .slots(slots)
                .selectedDate(date)
                .errorMessage("선택한 구간에 이미 마감되었거나 예약 불가한 시간이 포함되어 있습니다.")
                .build();
        }

        return ReservationStepResponse.builder()
            .step("confirm")
            .reservationType(ReservationType.SAME_DAY)
            .checkIn(from.getStartAt())
            .checkOut(to.getEndAt())
            .build();
    }

    private List<PackageDayResponse> buildPackageDays(Long placeId) {
        PlaceSlotPeriodResponse period = placeAvailabilityMapper.findSlotPeriodByPlaceId(placeId);
        if (period == null || period.getFromDate() == null || period.getToDate() == null) {
            return Collections.emptyList();
        }

        LocalDateTime fromAt = period.getFromDate().atStartOfDay();
        LocalDateTime toAt = period.getToDate().plusDays(1).atStartOfDay();

        List<PlaceSlotResponse> slots = placeAvailabilityMapper.findSlotsByPlaceAndPeriod(placeId, fromAt, toAt);

        Map<LocalDate, List<PlaceSlotResponse>> slotsByDate = new HashMap<>();

        for (int i=0; i<slots.size(); i++) {
            PlaceSlotResponse slot = slots.get(i);
            LocalDate date = slot.getStartAt().toLocalDate();

            if (!slotsByDate.containsKey(date)) {
                slotsByDate.put(date, new ArrayList<>());
            }
            slotsByDate.get(date).add(slot);
        }

        List<PackageDayResponse> days = new ArrayList<>();
        List<LocalDate> sortedDates = new ArrayList<>(slotsByDate.keySet());
        Collections.sort(sortedDates);

        for (int i=0; i<sortedDates.size(); i++) {
            LocalDate date = sortedDates.get(i);
            List<PlaceSlotResponse> daySlots = slotsByDate.get(date);

            boolean allOpen = true;
            for (int j=0; j<daySlots.size(); j++) {
                if (!"OPEN".equals(daySlots.get(j).getStatus())) {
                    allOpen = false;
                    break;
                }
            }

            days.add(PackageDayResponse.builder()
                .date(date)
                .selectable(allOpen)
                .build());
        }

        return days;
    }

    private PackageDayResponse findDay(List<PackageDayResponse> days, String dateStr) {
        LocalDate target = LocalDate.parse(dateStr);

        for (int i=0; i<days.size(); i++) {
            if (days.get(i).getDate().equals(target)) {
                return days.get(i);
            }
        }
        throw new BusinessException(ReservationErrorCode.SLOT_NOT_AVAILABLE);
    }

    private ReservationStepResponse validateAndBuildDayRange(Place place, Long placeId, List<PackageDayResponse> days, PackageDayResponse startDay, PackageDayResponse endDay) {
        PackageDayResponse from;
        PackageDayResponse to;

        if (startDay.getDate().isBefore(endDay.getDate())) {
            from = startDay;
            to = endDay;
        } else {
            from = endDay;
            to = startDay;
        }

        if (from.getDate().isEqual(to.getDate())) {
            return ReservationStepResponse.builder()
                .step("package-day")
                .reservationType(ReservationType.OVERNIGHT)
                .days(days)
                .startDay(startDay)
                .errorMessage("체크아웃 날짜는 체크인 다음 날 이후로 선택해주세요.")
                .build();
        }

        LocalTime checkInTime = place.getPackageCheckInTime() != null ? place.getPackageCheckInTime() : PlaceAvailabilityServiceImpl.DEFAULT_PACKAGE_CHECK_IN_TIME;
        LocalTime checkOutTime = place.getPackageCheckOutTime() != null ? place.getPackageCheckOutTime() : PlaceAvailabilityServiceImpl.DEFAULT_PACKAGE_CHECK_OUT_TIME;

        LocalDateTime checkIn = from.getDate().atTime(checkInTime);
        LocalDateTime checkOut = to.getDate().atTime(checkOutTime);

        List<PlaceSlotResponse> actualSlots = placeAvailabilityMapper.findSlotsByPlaceAndPeriod(placeId, checkIn, checkOut);

        long totalHours = Duration.between(checkIn, checkOut).toHours();
        long expectedSlotCount = totalHours / SLOT_HOURS;
        boolean blockedSlot = false;

        if (actualSlots.size() != expectedSlotCount) {
            blockedSlot = true;
        } else {
            for (int i = 0; i < actualSlots.size(); i++) {
                if (!"OPEN".equals(actualSlots.get(i).getStatus())) {
                    blockedSlot = true;
                    break;
                }
            }
        }

        if (blockedSlot) {
            return ReservationStepResponse.builder()
                .step("package-day")
                .reservationType(ReservationType.OVERNIGHT)
                .days(days)
                .errorMessage("예약 불가한 날짜가 포함되어 있습니다.")
                .build();
        }

        return ReservationStepResponse.builder()
            .step("confirm")
            .reservationType(ReservationType.OVERNIGHT)
            .checkIn(checkIn)
            .checkOut(checkOut)
            .build();
    }

	private BigDecimal calculateTotalPrice(Place place, ReservationType reservationType, LocalDateTime checkIn, LocalDateTime checkOut) {
		BigDecimal totalPrice;
		if (reservationType == ReservationType.SAME_DAY) {
			BigDecimal hourlyPrice = place.getHourlyPrice();
			if (hourlyPrice == null) {
				throw new BusinessException(ReservationErrorCode.HOURLY_PRICE_NOT_SET);
			}
			long totalHours = Duration.between(checkIn, checkOut).toHours();
			totalPrice = place.getHourlyPrice().multiply(BigDecimal.valueOf(totalHours));
		} else {
			BigDecimal nightlyPrice = place.getNightlyPrice();
			if (nightlyPrice == null) {
				throw new BusinessException(ReservationErrorCode.NIGHTLY_PRICE_NOT_SET);
			}
			long totalDays = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
			totalPrice = place.getNightlyPrice().multiply(BigDecimal.valueOf(totalDays));
		}
		return totalPrice;
	}
}
