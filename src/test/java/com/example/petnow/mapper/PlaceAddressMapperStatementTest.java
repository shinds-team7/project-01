package com.example.petnow.mapper;

import com.example.petnow.entity.PlaceAddress;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceAddressMapperStatementTest {

    private static final String MAPPER_RESOURCE = "mapper/PlaceAddressMapper.xml";
    private static final String NAMESPACE = "com.example.petnow.mapper.PlaceAddressMapper.";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(MAPPER_RESOURCE)) {
            assertThat(in).as("매퍼 XML을 찾지 못했다: %s", MAPPER_RESOURCE).isNotNull();
            new XMLMapperBuilder(
                    in, configuration, MAPPER_RESOURCE, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void mapsTheWholePlaceAddressEntity() {
        MappedStatement statement = configuration.getMappedStatement(NAMESPACE + "findByPlaceId");

        assertThat(statement.getResultMaps().get(0).getType()).isEqualTo(PlaceAddress.class);
        assertThat(collapse(statement.getBoundSql(41L).getSql()))
                .contains("latitude", "longitude", "road_address AS roadAddress")
                .contains("WHERE place_id = ?");
    }

    @Test
    void selectsOnlyMissingCoordinatesWithinTheBatchLimit() {
        BoundSql boundSql = configuration
                .getMappedStatement(NAMESPACE + "findWithoutCoordinates")
                .getBoundSql(Map.of("limit", 100));

        assertThat(collapse(boundSql.getSql()))
                .contains("WHERE latitude IS NULL OR longitude IS NULL")
                .contains("ORDER BY id LIMIT ?");
        assertThat(boundSql.getParameterMappings())
                .extracting(mapping -> mapping.getProperty())
                .containsExactly("limit");
    }

    @Test
    void updatesLatitudeAndLongitudeByPlaceId() {
        BoundSql boundSql = configuration
                .getMappedStatement(NAMESPACE + "updateCoordinates")
                .getBoundSql(Map.of(
                        "placeId", 41L,
                        "latitude", new BigDecimal("37.5446397"),
                        "longitude", new BigDecimal("127.0557550")));

        assertThat(collapse(boundSql.getSql()))
                .isEqualTo("UPDATE place_addresses SET latitude = ?, longitude = ? WHERE place_id = ?");
        assertThat(boundSql.getParameterMappings())
                .extracting(mapping -> mapping.getProperty())
                .containsExactly("latitude", "longitude", "placeId");
    }

    private String collapse(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
