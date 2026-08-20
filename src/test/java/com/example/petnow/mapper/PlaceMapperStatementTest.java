package com.example.petnow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import com.example.petnow.dto.request.PlaceFilterCriteria;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 목록 조회가 좌표를 어떻게 가져오는지 고정한다. (#277)
 *
 * <p>막으려는 회귀는 조인 종류 하나다. {@code place_addresses} 를 {@code INNER JOIN} 으로 붙이면
 * 주소 행이 없거나 아직 지오코딩되지 않은 장소가 목록에서 통째로 사라진다. 예외도 로그도 없이
 * 그냥 몇 곳이 안 보이는 형태라 눈치채기 어렵다. 지도에 못 찍는 것과 목록에서 빠지는 것은 다르다.
 *
 * <p>DB 없이 매퍼 XML 만 읽어 검사한다. 실제 조회 결과까지 보는 통합 테스트는 Testcontainers 로
 * 넘어간 뒤에 붙이는 편이 낫다.
 */
class PlaceMapperStatementTest {

    private static final String PLACE_MAPPER = "mapper/PlaceMapper.xml";
    private static final String FIND_ALL_PUBLISHED =
            "com.example.petnow.mapper.PlaceMapper.findAllPublished";
    /** /nearby 가 실제로 타는 쿼리다. 여기에 좌표가 없으면 지도에 마커가 하나도 찍히지 않는다. (#7) */
    private static final String FIND_BY_FILTER =
            "com.example.petnow.mapper.PlaceMapper.findByFilter";
    private static final String FIND_DETAIL_BY_ID =
            "com.example.petnow.mapper.PlaceMapper.findDetailById";
    private static final String UPSERT_ADDRESS =
            "com.example.petnow.mapper.PlaceMapper.upsertAddress";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        // application.yaml 의 mybatis.configuration 과 같은 조건에서 읽는다.
        configuration.setMapUnderscoreToCamelCase(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(PLACE_MAPPER)) {
            assertThat(in).as("매퍼 XML 을 찾지 못했다: %s", PLACE_MAPPER).isNotNull();
            new XMLMapperBuilder(in, configuration, PLACE_MAPPER, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    @DisplayName("findAllPublished 는 좌표를 LEFT JOIN 으로 가져온다")
    void findAllPublishedKeepsPlacesWithoutAddress() {
        String sql = sqlOf(FIND_ALL_PUBLISHED);

        assertThat(sql).contains("left join place_addresses");
        assertThat(sql).doesNotContain("inner join place_addresses");
        // 조인만 있고 join 앞에 left 가 빠진 형태도 INNER JOIN 이다
        assertThat(sql).doesNotContain(", place_addresses");
    }

    @Test
    @DisplayName("findAllPublished 가 위경도를 목록 응답에 실어 준다")
    void findAllPublishedSelectsCoordinates() {
        String sql = sqlOf(FIND_ALL_PUBLISHED);

        assertThat(sql).contains("latitude");
        assertThat(sql).contains("longitude");
    }

    @Test
    @DisplayName("findByFilter 도 위경도를 실어 준다")
    void findByFilterSelectsCoordinates() {
        // 조건 필터링(#7)이 붙은 뒤로 /nearby 는 findAllPublished 가 아니라 이 쿼리를 탄다.
        // 여기에 좌표를 안 실으면 조건을 걸고 들어온 화면만 조용히 마커가 0개가 된다.
        String sql = sqlOf(FIND_BY_FILTER, PlaceFilterCriteria.builder()
                .scheduleMode(PlaceFilterCriteria.ScheduleMode.NONE)
                .build());

        assertThat(sql).contains("pa.latitude");
        assertThat(sql).contains("pa.longitude");
        assertThat(sql).contains("left join place_addresses");
        assertThat(sql).doesNotContain("inner join place_addresses");
    }

    @Test
    @DisplayName("장소 상세는 저장된 평균 별점과 삭제되지 않은 리뷰 수를 가져온다")
    void findDetailSelectsRatingSummary() {
        String sql = sqlOf(FIND_DETAIL_BY_ID, Map.of("placeId", 3L));

        assertThat(sql)
                .contains("average_rating as averagerating")
                .contains("as reviewcount")
                .contains("rv.deleted_at is null");
    }

    @Test
    @DisplayName("주소 수정 시 이전 좌표를 초기화한다")
    void upsertAddressClearsStaleCoordinates() {
        String sql = sqlOf(UPSERT_ADDRESS, Map.of(
                "placeId", 3L,
                "sido", "서울특별시",
                "sigungu", "성동구",
                "roadAddress", "서울특별시 성동구 왕십리로 1"));

        assertThat(sql)
                .contains("latitude = null")
                .contains("longitude = null");
    }

    /** 공백을 하나로 줄이고 소문자로 낮춰 비교한다. 줄바꿈·들여쓰기가 바뀌어도 테스트가 흔들리지 않게. */
    private String sqlOf(String statementId) {
        return sqlOf(statementId, null);
    }

    /** 동적 SQL 은 조건 객체가 있어야 {@code <if>} 가 풀린다. */
    private String sqlOf(String statementId, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(statementId);
        assertThat(statement).as("%s 를 찾지 못했다", statementId).isNotNull();
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
