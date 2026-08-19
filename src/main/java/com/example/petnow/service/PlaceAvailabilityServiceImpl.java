package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceOperatingPolicyUpdateRequest;
import com.example.petnow.dto.response.PlaceSlotResponse;
import com.example.petnow.dto.response.PlaceSlotPeriodResponse;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceAvailability;
import com.example.petnow.entity.SlotStatus;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.mapper.PlaceAvailabilityMapper;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceAvailabilityServiceImpl implements PlaceAvailabilityService {

    public static final int SLOT_HOURS = 3;
    public static final LocalTime DEFAULT_PACKAGE_CHECK_IN_TIME = LocalTime.of(15, 0);
    public static final LocalTime DEFAULT_PACKAGE_CHECK_OUT_TIME = LocalTime.of(12, 0);

    private static final int SLOTS_PER_DAY = 24 / SLOT_HOURS;
    private static final int INSERT_CHUNK_SIZE = 500;
    private static final int MAX_SLOT_PERIOD_DAYS = 90;

    private final PlaceAvailabilityMapper placeAvailabilityMapper;
    private final PlaceMapper placeMapper;

    @Override
    @Transactional
    public int createSlots(Long hostUserId, Long placeId, LocalDate fromDate, LocalDate toDate) {
        validateOwner(hostUserId, placeId);
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)
                || ChronoUnit.DAYS.between(fromDate, toDate) + 1 > MAX_SLOT_PERIOD_DAYS) {
            throw new BusinessException(PlaceErrorCode.PLACE_AVAILABILITY_PERIOD_INVALID);
        }

        List<PlaceAvailability> slots = new ArrayList<>();
        int insertedCount = 0;
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            for (int index = 0; index < SLOTS_PER_DAY; index++) {
                LocalDateTime startAt = date.atStartOfDay().plusHours((long) index * SLOT_HOURS);
                slots.add(PlaceAvailability.builder()
                        .placeId(placeId)
                        .startAt(startAt)
                        .endAt(startAt.plusHours(SLOT_HOURS))
                        .status(SlotStatus.OPEN)
                        .build());

                if (slots.size() == INSERT_CHUNK_SIZE) {
                    insertedCount += placeAvailabilityMapper.insertSlots(slots);
                    slots.clear();
                }
            }
        }

        if (!slots.isEmpty()) {
            insertedCount += placeAvailabilityMapper.insertSlots(slots);
        }
        return insertedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceSlotResponse> getHostSlots(Long hostUserId, Long placeId, LocalDate date) {
        validateOwner(hostUserId, placeId);
        return findSlotsOfDay(placeId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceSlotPeriodResponse getSlotPeriod(Long hostUserId, Long placeId) {
        validateOwner(hostUserId, placeId);
        return placeAvailabilityMapper.findSlotPeriodByPlaceId(placeId);
    }

    @Override
    @Transactional
    public void changeSlotStatus(Long hostUserId, Long placeId, Long slotId, SlotStatus status) {
        validateOwner(hostUserId, placeId);
        if (status != SlotStatus.OPEN && status != SlotStatus.BLOCKED) {
            throw new BusinessException(PlaceErrorCode.PLACE_SLOT_STATUS_INVALID);
        }
        if (placeAvailabilityMapper.updateSlotStatus(placeId, slotId, status) != 1) {
            throw new BusinessException(PlaceErrorCode.PLACE_SLOT_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional
    public void updateOperatingPolicy(Long hostUserId,
                                      Long placeId,
                                      PlaceOperatingPolicyUpdateRequest request) {
        validateOwner(hostUserId, placeId);
        LocalTime checkInTime = request.getPackageCheckInTime();
        LocalTime checkOutTime = request.getPackageCheckOutTime();

        if (request.isSupportsPackage()) {
            checkInTime = checkInTime == null ? DEFAULT_PACKAGE_CHECK_IN_TIME : checkInTime;
            checkOutTime = checkOutTime == null ? DEFAULT_PACKAGE_CHECK_OUT_TIME : checkOutTime;
            if (!isOnGrid(checkInTime) || !isOnGrid(checkOutTime)) {
                throw new BusinessException(PlaceErrorCode.PLACE_PACKAGE_TIME_INVALID);
            }
        } else {
            checkInTime = null;
            checkOutTime = null;
        }

        int updatedCount = placeMapper.updateOperatingPolicy(
                placeId,
                request.isSupportsHourly(),
                request.isSupportsPackage(),
                checkInTime,
                checkOutTime
        );
        if (updatedCount != 1) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
    }

    public static boolean isOnGrid(LocalTime time) {
        return time != null
                && time.getMinute() == 0
                && time.getSecond() == 0
                && time.getNano() == 0
                && time.getHour() % SLOT_HOURS == 0;
    }

    private List<PlaceSlotResponse> findSlotsOfDay(Long placeId, LocalDate date) {
        return placeAvailabilityMapper.findSlotsByPlaceAndPeriod(
                placeId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    private void validateOwner(Long hostUserId, Long placeId) {
        Place place = findPlace(placeId);
        if (hostUserId == null || !hostUserId.equals(place.getHostUserId())) {
            throw new BusinessException(PlaceErrorCode.PLACE_ACCESS_DENIED);
        }
    }

    private Place findPlace(Long placeId) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        return place;
    }
}
