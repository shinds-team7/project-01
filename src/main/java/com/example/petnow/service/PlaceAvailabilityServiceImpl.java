package com.example.petnow.service;

import com.example.petnow.dto.response.PackageDayResponse;
import com.example.petnow.dto.response.PlaceSlotResponse;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceAvailabilityServiceImpl implements PlaceAvailabilityService {

    /**
     * 예약 재고의 최소 단위. 3시간 격자이므로 하루는 00시부터 8칸으로 나뉜다.
     * 이 값을 바꾸면 이미 생성된 슬롯과 격자가 어긋나므로, 변경 시 기존 슬롯을 재생성해야 한다.
     */
    public static final int SLOT_HOURS = 3;
    private static final int SLOTS_PER_DAY = 24 / SLOT_HOURS;
    private static final int INSERT_CHUNK_SIZE = 500;

    private final PlaceAvailabilityMapper placeAvailabilityMapper;
    private final PlaceMapper placeMapper;

    /**
     * 지정한 날짜 범위를 1시간 격자로 연다.
     * 패키지 예약이 밤을 넘어야 하므로 운영시간 밖도 OPEN 으로 만든다.
     * 시 예약의 운영시간 제한은 조회와 예약 검증에서 처리한다.
     */
    @Override
    @Transactional
    public int openSlots(Long placeId, LocalDate fromDate, LocalDate toDate) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        List<PlaceAvailability> buffer = new ArrayList<>();
        int inserted = 0;

        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            for (int index = 0; index < SLOTS_PER_DAY; index++) {
                LocalDateTime startAt = date.atStartOfDay().plusHours((long) index * SLOT_HOURS);
                buffer.add(PlaceAvailability.builder()
                        .placeId(placeId)
                        .startAt(startAt)
                        .endAt(startAt.plusHours(SLOT_HOURS))
                        .status(SlotStatus.OPEN)
                        .build());

                if (buffer.size() >= INSERT_CHUNK_SIZE) {
                    inserted += placeAvailabilityMapper.insertSlots(buffer);
                    buffer.clear();
                }
            }
        }

        if (!buffer.isEmpty()) {
            inserted += placeAvailabilityMapper.insertSlots(buffer);
        }
        return inserted;
    }

    /**
     * 시 예약 화면용. 하루 00시부터 24시까지의 슬롯을 그대로 내려준다.
     * 운영시간으로 걸러내지 않으므로 심야 시간대도 호스트가 열어두면 예약할 수 있다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlaceSlotResponse> getHourlySlots(Long placeId, LocalDate date) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        return placeAvailabilityMapper.findSlotsByPlaceAndPeriod(placeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    /**
     * 패키지 달력용. 그 날짜의 숙박 구간 슬롯이 전부 OPEN 일 때만 선택 가능으로 내려준다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PackageDayResponse> getPackageDays(Long placeId, YearMonth yearMonth) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        LocalTime checkInTime = place.getPackageCheckInTime() != null
                ? place.getPackageCheckInTime() : LocalTime.of(15, 0);
        LocalTime checkOutTime = place.getPackageCheckOutTime() != null
                ? place.getPackageCheckOutTime() : LocalTime.of(11, 0);

        List<PackageDayResponse> days = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            LocalDateTime startAt = date.atTime(checkInTime);
            LocalDateTime endAt = date.plusDays(1).atTime(checkOutTime);

            int required = (int) java.time.Duration.between(startAt, endAt).toHours() / SLOT_HOURS;
            int open = placeAvailabilityMapper.countOpenSlotsInRange(placeId, startAt, endAt);

            days.add(PackageDayResponse.builder()
                    .date(date)
                    .selectable(open == required)
                    .build());
        }
        return days;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceSlotResponse> getSlotsOfDay(Long placeId, LocalDate date) {
        return placeAvailabilityMapper.findSlotsByPlaceAndPeriod(placeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    @Override
    @Transactional
    public void changeSlotStatus(Long placeId, Long slotId, String status, Long hostUserId) {
        Place place = placeMapper.findById(placeId);
        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        if (!place.getHostUserId().equals(hostUserId)) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        placeAvailabilityMapper.updateSlotStatus(slotId, status);
    }
}
