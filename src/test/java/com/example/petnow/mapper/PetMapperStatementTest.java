package com.example.petnow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Locale;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.petnow.entity.PetPhoto;

/**
 * 반려동물 사진 저장의 선행 조건인 매퍼 설정을 고정한다. (#231)
 *
 * <p>막으려는 회귀는 두 가지다.
 * <ol>
 *   <li>{@code insertPet} 에서 {@code useGeneratedKeys} 가 빠지면 INSERT 후 {@code pet.getId()} 가
 *       null 로 남아 {@code pet_photos.pet_id} 를 채울 수 없다. 예외가 나지 않고 조용히 실패해서
 *       사진 저장 단계에 가서야 드러난다.</li>
 *   <li>{@code findByPetId} 에서 {@code LIMIT} 이 빠지면 한 마리에 사진 행이 2개가 되는 순간
 *       반환 타입이 단건이라 {@code TooManyResultsException} 으로 조회가 죽는다.</li>
 * </ol>
 *
 * <p>DB 없이 매퍼 XML 만 읽어서 검사한다. 실제 INSERT 로 PK 가 채워지는지 확인하는 통합 테스트는
 * 테스트용 DataSource 가 정해진 뒤(#238) 붙이는 편이 낫다. 지금 H2 를 끌어오면 그 결정을 앞질러
 * 버리고, 클린 클론에서 테스트가 도는지 여부도 그 이슈와 얽힌다.
 */
class PetMapperStatementTest {

    private static final String PET_MAPPER = "mapper/PetMapper.xml";
    private static final String PET_PHOTO_MAPPER = "mapper/PetPhotoMapper.xml";

    private Configuration configuration;

    @BeforeEach
    void loadMapperXml() throws Exception {
        configuration = new Configuration();
        // application.yaml 의 mybatis.configuration 과 같은 조건에서 읽는다.
        configuration.setMapUnderscoreToCamelCase(true);
        parse(PET_MAPPER);
        parse(PET_PHOTO_MAPPER);
    }

    private void parse(String resource) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(in).as("매퍼 XML 을 찾지 못했다: %s", resource).isNotNull();
            new XMLMapperBuilder(in, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    @DisplayName("insertPet 은 생성된 PK 를 Pet.id 로 돌려준다")
    void insertPetReturnsGeneratedKey() {
        MappedStatement insertPet =
                configuration.getMappedStatement("com.example.petnow.mapper.PetMapper.insertPet");

        assertThat(insertPet.getKeyGenerator())
                .as("useGeneratedKeys 가 없으면 INSERT 후 pet.getId() 가 null 이라 사진을 저장할 수 없다")
                .isInstanceOf(Jdbc3KeyGenerator.class);
        assertThat(insertPet.getKeyProperties()).containsExactly("id");
    }

    @Test
    @DisplayName("findByPetId 는 사진 행이 여러 개여도 한 건만 가져온다")
    void findByPetIdTakesOnlyOneRow() {
        MappedStatement findByPetId =
                configuration.getMappedStatement("com.example.petnow.mapper.PetPhotoMapper.findByPetId");

        assertThat(findByPetId.getResultMaps().get(0).getType())
                .as("반환 타입이 단건이라서 LIMIT 이 필요하다")
                .isEqualTo(PetPhoto.class);
        assertThat(normalize(findByPetId.getBoundSql(1L).getSql()))
                .contains("LIMIT 1")
                // 정렬 기준이 같은 행이 있어도 어느 행이 나올지 흔들리지 않아야 한다.
                .contains("ORDER BY SORT_ORDER, ID");
    }

    private String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
    }
}
