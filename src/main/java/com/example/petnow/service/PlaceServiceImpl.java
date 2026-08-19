package com.example.petnow.service;

import com.example.petnow.dto.request.PlaceCreateRequest;
import com.example.petnow.dto.request.PlaceFilterCriteria;
import com.example.petnow.dto.request.PlaceFilterRequest;
import com.example.petnow.dto.response.PlaceDetailResponse;
import com.example.petnow.dto.response.PetListResponse;
import com.example.petnow.dto.response.PlaceListResponse;
import com.example.petnow.dto.response.PlaceSearchResponse;
import com.example.petnow.entity.Pet;
import com.example.petnow.entity.Place;
import com.example.petnow.entity.PlaceStatus;
import com.example.petnow.entity.User;
import com.example.petnow.exception.AuthErrorCode;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.PlaceErrorCode;
import com.example.petnow.exception.ReservationErrorCode;
import com.example.petnow.exception.UserErrorCode;
import com.example.petnow.mapper.AuthMapper;
import com.example.petnow.mapper.PetMapper;
import com.example.petnow.mapper.PlaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {

    private static final String SEOUL = "서울특별시";

    private final PlaceMapper placeMapper;
    private final AuthMapper authMapper;
    private final PetMapper petMapper;

    @Override
    @Transactional
    public void createPlace(Long userId, PlaceCreateRequest request) {
        User user = authMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        Place place = Place.builder()
                .hostUserId(userId)
                .hostUserNickname(user.getNickname())
                .name(request.getName())
                .description(request.getDescription())
                .placeType(request.getPlaceType())
                .areaSize(request.getAreaSize())
                .capacity(request.getCapacity())
                .allowsSmallDog(request.isAllowsSmallDog())
                .allowsMediumDog(request.isAllowsMediumDog())
                .allowsLargeDog(request.isAllowsLargeDog())
                .providesHomeCamera(request.isProvidesHomeCamera())
                .providesRealtimePhoto(request.isProvidesRealtimePhoto())
                .providesYard(request.isProvidesYard())
                .providesWalk(request.isProvidesWalk())
                .otherOptions(request.isOtherOptionsEnabled() ? request.getOtherOptions() : null)
                .hourlyPrice(request.getHourlyPrice())
                .nightlyPrice(request.getNightlyPrice())
                .status(PlaceStatus.PUBLISHED)
                .build();

        int result = placeMapper.insert(place);
        if (result != 1 || place.getId() == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_CREATE_FAILED);
        }

        int addressResult = placeMapper.insertAddress(
                place.getId(), SEOUL, request.getSigungu(), request.getRoadAddress());
        if (addressResult != 1) {
            throw new BusinessException(PlaceErrorCode.PLACE_CREATE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceListResponse> getPublishedPlaces() {
        return placeMapper.findAllPublished();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId, Long loginUserId) {
        PlaceDetailResponse place = placeMapper.findDetailById(placeId);

        if (place == null) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        boolean owner = loginUserId != null && loginUserId.equals(place.getHostUserId());
        boolean publiclyAccessible = place.isVisible()
                && place.getStatus() == PlaceStatus.PUBLISHED;

        if (!owner && !publiclyAccessible) {
            throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
        }

        return place;
    }

    // ───────────────────────── 조건 필터링 (#7) ─────────────────────────

    /** 라벨용 서식. "8월 20일", "09:00" 처럼 화면에 그대로 나가는 문구를 만든다. */
    private static final DateTimeFormatter DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter TIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<String> getFilterRegions() {
        return placeMapper.findFilterableSigungu();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceSearchResponse searchPlaces(Long loginUserId, PlaceFilterRequest request) {
        List<PetListResponse> selectedPets = resolveSelectedPets(loginUserId, request.getPetIds());

        return PlaceSearchResponse.builder()
                .places(placeMapper.findByFilter(toCriteria(request, selectedPets)))
                .regionLabel(regionLabel(request.getRegions()))
                .dateLabel(dateLabel(request))
                .timeLabel(timeLabel(request))
                .petLabel(petLabel(selectedPets))
                .build();
    }

    /**
     * 고른 반려견을 확정한다. 중복은 여기서 걷어내고(체크박스라 화면에서는 안 나지만
     * 주소창으로 직접 보낼 수 있다), 남의 아이나 없는 아이가 섞이면 조용히 무시하지 않고
     * 예약 생성과 같은 관례로 예외를 던진다.
     */
    private List<PetListResponse> resolveSelectedPets(Long loginUserId, List<Long> petIds) {
        Set<Long> wanted = new LinkedHashSet<>();
        if (petIds != null) {
            for (Long petId : petIds) {
                if (petId != null) {
                    wanted.add(petId);
                }
            }
        }
        if (wanted.isEmpty()) {
            return List.of();
        }
        // 컨트롤러가 로그인 화면으로 먼저 보내므로 여기까지 오면 이미 로그인 상태다.
        if (loginUserId == null) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }

        List<PetListResponse> selected = petMapper.getPetList(loginUserId).stream()
                .filter(pet -> wanted.contains(pet.getId()))
                .toList();
        if (selected.size() != wanted.size()) {
            throw new BusinessException(ReservationErrorCode.PET_NOT_FOUND);
        }
        return selected;
    }

    /**
     * 화면에서 온 값을 매퍼가 읽을 조건으로 옮긴다.
     *
     * <p>일정은 네 모드 중 하나로만 나간다. 날짜가 없으면 슬롯 조건을 아예 걸지 않는데,
     * 호스트가 아직 일정을 열지 않은 장소까지 전부 사라지는 것을 막기 위해서다.
     */
    private PlaceFilterCriteria toCriteria(PlaceFilterRequest request, List<PetListResponse> pets) {
        Set<Pet.Size> sizes = new LinkedHashSet<>();
        for (PetListResponse pet : pets) {
            // 크기를 안 적은 아이는 조건을 만들 수 없다. 그 아이 때문에 결과가 비면 안 되므로 뺀다.
            if (pet.getSize() != null) {
                sizes.add(pet.getSize());
            }
        }

        PlaceFilterCriteria.PlaceFilterCriteriaBuilder criteria = PlaceFilterCriteria.builder()
                .regions(normalizeRegions(request.getRegions()))
                .placeType(request.getPlaceType())
                .petCount(pets.size())
                .requiresSmallDog(sizes.contains(Pet.Size.SMALL))
                .requiresMediumDog(sizes.contains(Pet.Size.MEDIUM))
                .requiresLargeDog(sizes.contains(Pet.Size.LARGE))
                .sort(request.getSort());

        if (!request.hasSchedule()) {
            return criteria.scheduleMode(PlaceFilterCriteria.ScheduleMode.NONE).build();
        }

        LocalDate startDate = request.getStartDate();
        if (request.isMultiDay()) {
            return criteria.scheduleMode(PlaceFilterCriteria.ScheduleMode.PACKAGE)
                    .startDate(startDate)
                    .endDate(request.resolvedEndDate())
                    .build();
        }
        if (request.getStartTime() == null) {
            return criteria.scheduleMode(PlaceFilterCriteria.ScheduleMode.DAY)
                    .startAt(startDate.atStartOfDay())
                    .endAt(startDate.plusDays(1).atStartOfDay())
                    .build();
        }

        int startMinute = request.startMinuteOfDay();
        int endMinute = request.endMinuteOfDay();
        LocalDateTime dayStart = startDate.atStartOfDay();
        return criteria.scheduleMode(PlaceFilterCriteria.ScheduleMode.HOURLY)
                .startAt(dayStart.plusMinutes(startMinute))
                // 종료 24:00 은 다음 날 0시다. atTime 으로는 표현할 수 없어 분으로 더한다.
                .endAt(dayStart.plusMinutes(endMinute))
                .requiredSlots((endMinute - startMinute) / 60 / PlaceAvailabilityServiceImpl.SLOT_HOURS)
                .build();
    }

    /** 빈 문자열·중복을 걷어낸 지역 목록. 남는 게 없으면 지역 조건을 걸지 않는다. */
    private static List<String> normalizeRegions(List<String> regions) {
        if (regions == null) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String region : regions) {
            if (region != null && !region.isBlank()) {
                normalized.add(region.trim());
            }
        }
        return List.copyOf(normalized);
    }

    private static String regionLabel(List<String> regions) {
        List<String> normalized = normalizeRegions(regions);
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.size() == 1
                ? normalized.get(0)
                : normalized.get(0) + " 외 " + (normalized.size() - 1) + "곳";
    }

    private static String dateLabel(PlaceFilterRequest request) {
        if (!request.hasSchedule()) {
            return null;
        }
        String start = request.getStartDate().format(DATE_LABEL_FORMAT);
        return request.isMultiDay()
                ? start + " ~ " + request.resolvedEndDate().format(DATE_LABEL_FORMAT)
                : start;
    }

    private static String timeLabel(PlaceFilterRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            return null;
        }
        // 종료 자정은 "00:00" 이 아니라 그날의 끝이므로 24:00 으로 적는다.
        String end = request.getEndTime().equals(LocalTime.MIDNIGHT)
                ? "24:00"
                : request.getEndTime().format(TIME_LABEL_FORMAT);
        return request.getStartTime().format(TIME_LABEL_FORMAT) + " ~ " + end;
    }

    private static String petLabel(List<PetListResponse> pets) {
        if (pets.isEmpty()) {
            return null;
        }
        String first = pets.get(0).getName();
        return pets.size() == 1 ? first : first + " 외 " + (pets.size() - 1) + "마리";
    }
}
