package com.example.petnow.mapper;

import com.example.petnow.dto.request.PlaceFilterCriteria;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 조건 필터링 쿼리가 실제로 어떤 SQL 이 되는지 고정한다. (#7)
 *
 * <p>DB 없이 매퍼 XML 만 읽어 동적 SQL 을 만들어 본다. Testcontainers 가 없어 진짜 조회
 * 결과까지는 못 보지만, 여기서 막고 싶은 회귀는 결과값이 아니라 <b>쿼리의 모양</b>이다.
 * <ol>
 *   <li>패키지 모드의 슬롯 수는 장소별 입·퇴실 시각으로 <b>서브쿼리 안에서</b> 계산해야 한다.
 *       자바에서 계산한 값을 파라미터로 넘기면 장소마다 다른 시각을 무시하게 된다.</li>
 *   <li>정렬은 허용 목록으로만 만들어야 한다. {@code ${sort}} 로 이어 붙이면 SQL 인젝션이다.</li>
 *   <li>일정을 안 고르면 슬롯 조건 자체가 없어야 한다. 슬롯을 아직 안 연 장소가 통째로
 *       사라지면 안 된다.</li>
 * </ol>
 */
class PlaceMapperFilterStatementTest {

    private static final String PLACE_MAPPER = "mapper/PlaceMapper.xml";
    private static final String FIND_BY_FILTER = "com.example.petnow.mapper.PlaceMapper.findByFilter";

    private static final LocalDate START_DATE = LocalDate.of(2026, 8, 20);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 22);

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
    @DisplayName("일정을 안 고르면 슬롯 테이블을 아예 보지 않는다")
    void noScheduleTouchesNoSlotTable() {
        String sql = sqlOf(base(PlaceFilterCriteria.ScheduleMode.NONE).build());

        assertThat(sql).doesNotContain("place_availability");
        assertThat(sql).contains("p.status = 'PUBLISHED'");
        assertThat(sql).contains("p.is_visible = TRUE");
        assertThat(sql).contains("p.deleted_at IS NULL");
    }

    @Test
    @DisplayName("시 예약은 고른 구간이 OPEN 슬롯으로 꽉 찼는지 센다")
    void hourlyCountsOpenSlots() {
        PlaceFilterCriteria criteria = base(PlaceFilterCriteria.ScheduleMode.HOURLY)
                .startAt(START_DATE.atTime(9, 0))
                .endAt(START_DATE.atTime(18, 0))
                .requiredSlots(3)
                .build();

        BoundSql boundSql = boundSqlOf(criteria);

        assertThat(collapse(boundSql.getSql())).contains("p.supports_hourly = TRUE");
        assertThat(collapse(boundSql.getSql()))
                .contains("(SELECT COUNT(*) FROM place_availability s WHERE s.place_id = p.id"
                        + " AND s.status = 'OPEN' AND s.start_at >= ? AND s.end_at <= ?) = ?");
        assertThat(properties(boundSql)).containsExactly("startAt", "endAt", "requiredSlots");
    }

    @Test
    @DisplayName("패키지는 장소별 입·퇴실 시각으로 서브쿼리 안에서 필요한 슬롯 수를 만든다")
    void packageComputesRequiredSlotsPerPlace() {
        PlaceFilterCriteria criteria = base(PlaceFilterCriteria.ScheduleMode.PACKAGE)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();

        BoundSql boundSql = boundSqlOf(criteria);
        String sql = collapse(boundSql.getSql());

        assertThat(sql).contains("p.supports_package = TRUE");
        assertThat(sql).contains("p.package_check_in_time IS NOT NULL");
        assertThat(sql).contains("p.package_check_out_time IS NOT NULL");
        // 구간의 시작·끝을 장소 컬럼과 묶어 만든다
        assertThat(sql).contains("s.start_at >= TIMESTAMP(?, p.package_check_in_time)");
        assertThat(sql).contains("s.end_at <= TIMESTAMP(?, p.package_check_out_time)");
        // 필요한 슬롯 수도 파라미터가 아니라 장소별 계산이다
        assertThat(sql).contains("= TIMESTAMPDIFF(HOUR,"
                + " TIMESTAMP(?, p.package_check_in_time),"
                + " TIMESTAMP(?, p.package_check_out_time)) / 3");
        assertThat(properties(boundSql))
                .containsExactly("startDate", "endDate", "startDate", "endDate")
                .doesNotContain("requiredSlots");
    }

    @Test
    @DisplayName("시간을 안 고른 하루는 그날 열린 슬롯이 하나라도 있으면 통과한다")
    void daySearchUsesExists() {
        PlaceFilterCriteria criteria = base(PlaceFilterCriteria.ScheduleMode.DAY)
                .startAt(START_DATE.atStartOfDay())
                .endAt(START_DATE.plusDays(1).atStartOfDay())
                .build();

        String sql = collapse(sqlOf(criteria));

        assertThat(sql).contains("AND EXISTS (SELECT 1 FROM place_availability s");
        assertThat(sql).doesNotContain("COUNT(*)");
    }

    @Test
    @DisplayName("지역과 마릿수·크기 조건은 고른 것만 붙는다")
    void regionAndPetConditionsAreOptional() {
        String withoutPets = collapse(sqlOf(base(PlaceFilterCriteria.ScheduleMode.NONE).build()));
        assertThat(withoutPets).doesNotContain("pa.sigungu IN");
        assertThat(withoutPets).doesNotContain("p.capacity");

        PlaceFilterCriteria criteria = base(PlaceFilterCriteria.ScheduleMode.NONE)
                .regions(List.of("성동구", "광진구"))
                .petCount(2)
                .requiresSmallDog(true)
                .requiresLargeDog(true)
                .build();
        String sql = collapse(sqlOf(criteria));

        assertThat(sql).contains("AND pa.sigungu IN ( ? , ? )");
        assertThat(sql).contains("AND p.capacity >= ?");
        assertThat(sql).contains("AND p.allows_small_dog = TRUE");
        assertThat(sql).contains("AND p.allows_large_dog = TRUE");
        // 고르지 않은 크기는 조건이 되지 않는다
        assertThat(sql).doesNotContain("AND p.allows_medium_dog = TRUE");
    }

    @Test
    @DisplayName("정렬은 허용 목록으로만 만들어지고 모르는 값은 최신순으로 떨어진다")
    void sortIsAllowlisted() {
        assertThat(collapse(sqlOf(sorted("price"))))
                .endsWith("ORDER BY p.hourly_price ASC, p.id DESC");
        assertThat(collapse(sqlOf(sorted("rating"))))
                .endsWith("ORDER BY p.average_rating DESC, p.id DESC");
        assertThat(collapse(sqlOf(sorted(null))))
                .endsWith("ORDER BY p.id DESC");
    }

    @Test
    @DisplayName("정렬 값에 SQL 을 넣어도 쿼리에 그대로 박히지 않는다")
    void sortDoesNotAllowInjection() {
        String injection = "p.id; DROP TABLE places--";

        String sql = collapse(sqlOf(sorted(injection)));

        assertThat(sql).doesNotContain("DROP TABLE");
        assertThat(sql).endsWith("ORDER BY p.id DESC");
    }

    private PlaceFilterCriteria sorted(String sort) {
        return base(PlaceFilterCriteria.ScheduleMode.NONE).sort(sort).build();
    }

    private PlaceFilterCriteria.PlaceFilterCriteriaBuilder base(PlaceFilterCriteria.ScheduleMode mode) {
        return PlaceFilterCriteria.builder()
                .regions(List.of())
                .scheduleMode(mode);
    }

    private String sqlOf(PlaceFilterCriteria criteria) {
        return boundSqlOf(criteria).getSql();
    }

    private BoundSql boundSqlOf(PlaceFilterCriteria criteria) {
        return configuration.getMappedStatement(FIND_BY_FILTER).getBoundSql(criteria);
    }

    /** 값이 ? 로 나가는 순서. 자리표시자와 파라미터가 어긋나면 여기서 드러난다. */
    private List<String> properties(BoundSql boundSql) {
        return boundSql.getParameterMappings().stream()
                .map(ParameterMapping::getProperty)
                .toList();
    }

    /** 줄바꿈·들여쓰기를 지워 한 줄로 만든다. XML 서식이 바뀌어도 테스트가 깨지지 않게. */
    private String collapse(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    @Test
    @DisplayName("일정 조건이 없으면 슬롯 파라미터도 만들지 않는다")
    void noScheduleBindsNoSlotParameter() {
        BoundSql boundSql = boundSqlOf(base(PlaceFilterCriteria.ScheduleMode.NONE).build());

        assertThat(properties(boundSql)).isEmpty();
        assertThat(boundSql.getSql()).doesNotContain("?");
    }

    @Test
    @DisplayName("패키지 파라미터는 날짜만 나가고 시각은 장소 컬럼에서 온다")
    void packageBindsOnlyDates() {
        BoundSql boundSql = boundSqlOf(base(PlaceFilterCriteria.ScheduleMode.PACKAGE)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build());

        List<Object> values = boundSql.getParameterMappings().stream()
                .map(mapping -> (Object) mapping.getJavaType())
                .toList();

        assertThat(values).containsOnly(LocalDate.class);
        assertThat(values).hasSize(4);
        assertThat(boundSql.getSql()).doesNotContain(LocalDateTime.class.getName());
    }
}
