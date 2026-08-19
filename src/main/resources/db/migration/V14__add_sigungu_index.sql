-- 장소 필터의 지역구 복수 선택 조건(pa.sigungu IN (...))을 지원한다.
-- 운영 데이터베이스에는 마이그레이션 시점에 기존 장소가 없어 주소 백필 대상도 없다.
CREATE INDEX `ix_place_addresses_sigungu`
    ON `place_addresses` (`sigungu`);
