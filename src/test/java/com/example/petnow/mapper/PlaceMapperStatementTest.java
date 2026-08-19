package com.example.petnow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;

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

    /** 공백을 하나로 줄이고 소문자로 낮춰 비교한다. 줄바꿈·들여쓰기가 바뀌어도 테스트가 흔들리지 않게. */
    private String sqlOf(String statementId) {
        MappedStatement statement = configuration.getMappedStatement(statementId);
        assertThat(statement).as("%s 를 찾지 못했다", statementId).isNotNull();
        return statement.getBoundSql(null).getSql()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
