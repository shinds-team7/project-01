package com.example.petnow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationMapperStatementTest {

    private static final String RESERVATION_MAPPER = "mapper/ReservationMapper.xml";
    private static final String NAMESPACE = "com.example.petnow.mapper.ReservationMapper.";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESERVATION_MAPPER)) {
            assertThat(in).as("매퍼 XML 을 찾지 못했다: %s", RESERVATION_MAPPER).isNotNull();
            new XMLMapperBuilder(in, configuration, RESERVATION_MAPPER, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    @DisplayName("게스트와 호스트 예약 목록이 장소 ID를 응답에 싣는다")
    void reservationListsSelectPlaceId() {
        assertThat(sqlOf("viewReservationList", Map.of("userId", 1L)))
                .contains("r.place_id as placeid");
        assertThat(sqlOf("viewReservationListByHost", Map.of("loginUserId", 1L)))
                .contains("r.place_id as placeid");
    }

    private String sqlOf(String statementName, Object parameter) {
        MappedStatement statement = configuration.getMappedStatement(NAMESPACE + statementName);
        return statement.getBoundSql(parameter).getSql()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
