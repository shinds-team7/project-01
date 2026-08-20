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
 * 호스트 홈 리뷰 탭이 타는 쿼리의 범위를 고정한다.
 *
 * <p>막으려는 회귀는 두 가지다.
 * <ol>
 *   <li>{@code p.host_user_id} 조건이 빠지면 호스트 화면에 남의 장소 리뷰까지 딸려 온다.
 *       화면은 멀쩡히 그려지고 개수만 늘어나서 눈으로는 알아채기 어렵다.</li>
 *   <li>{@code deleted_at} 조건이 빠지면 지운 리뷰나 내린 장소가 되살아난다.</li>
 * </ol>
 *
 * <p>DB 없이 매퍼 XML 만 읽어 검사한다. {@link PlaceMapperStatementTest} 와 같은 방식이다.
 */
class ReviewMapperStatementTest {

    private static final String REVIEW_MAPPER = "mapper/ReviewMapper.xml";
    private static final String FIND_BY_HOST =
            "com.example.petnow.mapper.ReviewMapper.findReviewsByHostUserId";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        // application.yaml 의 mybatis.configuration 과 같은 조건에서 읽는다.
        configuration.setMapUnderscoreToCamelCase(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(REVIEW_MAPPER)) {
            assertThat(in).as("매퍼 XML 을 찾지 못했다: %s", REVIEW_MAPPER).isNotNull();
            new XMLMapperBuilder(in, configuration, REVIEW_MAPPER, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    @DisplayName("호스트 리뷰 조회는 내 장소로만 범위를 좁힌다")
    void findReviewsByHostUserIdScopesToOwnPlaces() {
        String sql = sqlOf(FIND_BY_HOST);

        assertThat(sql).contains("p.host_user_id = ?");
    }

    @Test
    @DisplayName("호스트 리뷰 조회는 지운 리뷰와 내린 장소를 뺀다")
    void findReviewsByHostUserIdSkipsSoftDeleted() {
        String sql = sqlOf(FIND_BY_HOST);

        assertThat(sql).contains("r.deleted_at is null");
        assertThat(sql).contains("p.deleted_at is null");
    }

    @Test
    @DisplayName("호스트 리뷰 조회는 최신순으로 고정이다")
    void findReviewsByHostUserIdOrdersByLatest() {
        // 이 화면에는 정렬 선택지가 없다. 정렬이 빠지면 순서가 DB 마음대로가 된다.
        String sql = sqlOf(FIND_BY_HOST);

        assertThat(sql).contains("order by r.created_at desc");
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
