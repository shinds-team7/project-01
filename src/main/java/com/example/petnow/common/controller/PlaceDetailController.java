package com.example.petnow.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 게스트가 둘러보는 장소 상세 및 예약(요청→결제→완료) 화면.
 * 아직 장소 조회/예약/결제 API가 없어, /places 목록에 있는 데모 카드와 동일한
 * 고정 데이터를 보여준다. 실제 결제·예약 저장은 이루어지지 않는다.
 */
@Controller
public class PlaceDetailController {

    public record DemoReview(String name, String rating, String when, String text) {}

    public record DemoPlace(
            String id, String name, String region, String walk, String rating, int reviewCount,
            int hourlyPrice, List<String> tags, String description, List<String> conditions,
            String hostName, String hostMeta, List<DemoReview> reviews) {}

    private static final Map<String, DemoPlace> PLACES = new LinkedHashMap<>();

    static {
        register(new DemoPlace("sunny", "햇살 가득한 마당", "마포구 연남동", "도보 5분", "4.98", 126, 5000,
                List.of("마당 있음", "고양이 가능", "실시간 사진"),
                "겁 많은 아이도 천천히 적응하는 조용한 단독주택이에요. 마당에서 자유롭게 뛰어놀 수 있고, 실내 CCTV로 언제든 확인하실 수 있어요.",
                List.of("주택", "38평", "소형견", "고양이", "최대 2마리"),
                "이서연 호스트", "호스팅 2년 · 응답률 98%",
                List.of(
                        new DemoReview("박준호", "5.0", "1주 전", "마당이 넓어서 우리 강아지가 정말 좋아했어요. 사진도 자주 보내주셔서 안심됐습니다."),
                        new DemoReview("김민재", "4.9", "2주 전", "깨끗하고 조용해서 예민한 아이도 편하게 지냈어요. 재예약 예정입니다."))));

        register(new DemoPlace("seongsu", "성수 조용한 단독주택", "성동구 성수동", "도보 7분", "4.92", 86, 7000,
                List.of("마당 있음", "중형견 가능", "CCTV"),
                "마당이 있는 조용한 단독주택입니다. 사회성 좋은 소·중형견을 반갑게 맞이해요. 실내 냉난방과 CCTV가 준비되어 있어 안심하고 맡기실 수 있습니다.",
                List.of("주택", "42평", "소형견", "중형견", "최대 2마리"),
                "김도윤 호스트", "호스팅 3년 · 응답률 100%",
                List.of(
                        new DemoReview("이서연", "5.0", "2주 전", "마당이 넓어서 우리 강아지가 정말 좋아했어요. 사진도 자주 보내주셔서 안심됐습니다."),
                        new DemoReview("박준호", "4.8", "1개월 전", "깔끔하고 조용해서 예민한 아이도 편하게 지냈어요. 재예약 예정입니다."))));

        register(new DemoPlace("songpa", "포근한 잠실 아파트", "송파구 잠실동", "도보 11분", "4.88", 74, 4500,
                List.of("1:1 돌봄", "매일 산책", "CCTV"),
                "한 번에 한 가족만 돌보는 차분한 프라이빗 케어예요. 매일 산책을 챙기고, 급여·투약 등 세세한 요청도 꼼꼼히 지켜드려요.",
                List.of("아파트", "28평", "소형견", "최대 1마리"),
                "정하은 호스트", "호스팅 1년 · 응답률 96%",
                List.of(new DemoReview("최우진", "4.9", "3일 전", "1:1로 봐주셔서 정말 안심하고 맡겼어요. 산책 사진도 매일 보내주셨습니다."))));

        register(new DemoPlace("mangwon", "조용한 망원 하루", "마포구 망원동", "도보 13분", "4.84", 53, 4000,
                List.of("고양이 가능", "복약 가능", "실시간 사진"),
                "낮잠 자리를 넉넉히 마련한 고양이 친화 공간이에요. 투약이 필요한 아이도 시간 맞춰 꼼꼼히 챙겨드립니다.",
                List.of("오피스텔", "24평", "고양이", "최대 2마리"),
                "한지수 호스트", "호스팅 1년 · 응답률 100%",
                List.of(new DemoReview("오세라", "4.9", "5일 전", "약 챙겨주는 것도 꼼꼼하시고, 사진도 자주 보내주셔서 좋았어요."))));

        register(new DemoPlace("seocho", "구름이네 캣 스테이", "서초구 서초동", "도보 16분", "4.79", 41, 5500,
                List.of("고양이 전용", "분리 공간", "복약 가능"),
                "캣타워와 숨숨집이 준비된 고양이 전용 돌봄 공간이에요. 아이별로 독립된 공간에서 스트레스 없이 지낼 수 있어요.",
                List.of("아파트", "30평", "고양이", "최대 3마리"),
                "문가영 호스트", "호스팅 2년 · 응답률 99%",
                List.of(new DemoReview("서동현", "4.7", "1주 전", "고양이 전용이라 그런지 아이가 스트레스를 덜 받는 것 같았어요."))));

        register(new DemoPlace("gwanak", "포근한 가족 돌봄", "관악구 봉천동", "도보 20분", "4.76", 38, 6000,
                List.of("마당 있음", "고양이 가능", "장기 돌봄"),
                "온 가족이 함께 생활하며 자연스럽게 돌보는 집이에요. 장기 위탁도 가능해 여행이나 출장 때 특히 좋아요.",
                List.of("주택", "35평", "소형견", "중형견", "고양이"),
                "임채원 호스트", "호스팅 4년 · 응답률 97%",
                List.of(new DemoReview("조은서", "4.8", "2주 전", "장기로 맡겼는데 매일 사진과 함께 근황을 알려주셔서 안심됐어요."))));
    }

    private static void register(DemoPlace place) {
        PLACES.put(place.id(), place);
    }

    static DemoPlace find(String id) {
        return PLACES.getOrDefault(id, PLACES.values().iterator().next());
    }

    @GetMapping("/places/{id}")
    public String detail(@PathVariable String id, Model model) {
        model.addAttribute("place", find(id));
        return "place-detail";
    }

    @GetMapping("/places/{id}/booking")
    public String booking(@PathVariable String id, Model model) {
        model.addAttribute("place", find(id));
        return "booking-request";
    }

    @GetMapping("/places/{id}/payment")
    public String payment(@PathVariable String id, Model model) {
        model.addAttribute("place", find(id));
        return "payment";
    }

    @GetMapping("/places/{id}/booking/success")
    public String bookingSuccess(@PathVariable String id, Model model) {
        model.addAttribute("place", find(id));
        return "booking-success";
    }
}
