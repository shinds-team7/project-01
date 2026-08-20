package com.example.petnow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import com.example.petnow.entity.ReviewSortType;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewMapperStatementTest {

    private static final String REVIEW_MAPPER = "mapper/ReviewMapper.xml";
    private static final String NAMESPACE = "com.example.petnow.mapper.ReviewMapper.";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(REVIEW_MAPPER)) {
            assertThat(in).as("매퍼 XML 을 찾지 못했다: %s", REVIEW_MAPPER).isNotNull();
            new XMLMapperBuilder(in, configuration, REVIEW_MAPPER, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    @DisplayName("내 리뷰와 장소 리뷰는 활성 작성자 닉네임을 응답에 싣는다")
    void reviewListsSelectActiveReviewerName() {
        assertReviewerJoin(sqlOf("findReviewsByMemberId", Map.of("memberId", 1L)));
        assertReviewerJoin(sqlOf("findReviewsByPlaceId", Map.of(
                "placeId", 3L,
                "sort", ReviewSortType.LATEST)));
    }

    private void assertReviewerJoin(String sql) {
        assertThat(sql)
                .contains("u.nickname as reviewername")
                .contains("left join users u")
                .contains("u.deleted_at is null");
    }

    private String sqlOf(String statementName, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(NAMESPACE + statementName);
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
