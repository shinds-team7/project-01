-- place_availability 를 3시간 격자 재고 테이블로 재정의한다.
--
-- 기존 컬럼 is_available 은 코드 어디에서도 쓰이지 않는 상태였다.
-- 예약 가능 여부는 '호스트가 열었는가'와 '이미 예약이 잡혔는가' 두 가지가 섞인 개념이라
-- BOOLEAN 하나로는 구분이 안 된다. status 로 통합한다.
--
--   OPEN     예약 가능
--   BLOCKED  호스트가 막음 (휴무 등)
--   RESERVED 예약이 점유 중
ALTER TABLE `place_availability`
    DROP COLUMN `is_available`,
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        COMMENT '슬롯 상태(OPEN/BLOCKED/RESERVED)';

-- 같은 place 에 같은 시각 슬롯이 두 개 생기면 재고가 어긋난다.
-- 슬롯 생성을 INSERT IGNORE 로 재실행 가능하게 만드는 근거이기도 하다.
ALTER TABLE `place_availability`
    ADD CONSTRAINT `uk_place_availability_slot` UNIQUE (`place_id`, `start_at`);

-- 화면은 '특정 place 의 특정 기간' 을 항상 함께 조회한다.
CREATE INDEX `ix_place_availability_lookup`
    ON `place_availability` (`place_id`, `start_at`, `status`);


-- 호스트 운영 정책.
--
-- 슬롯은 하루 00시부터 24시까지 전부 생성한다.
-- 패키지 예약이 밤을 넘어야 하고, 시 예약도 심야 시간대를 열어둘 수 있어야 하기 때문이다.
-- 어느 시간을 실제로 받을지는 호스트가 슬롯을 BLOCKED 로 막아 조절한다.
--
-- 패키지 입/퇴실 시각은 3시간 격자 경계(00, 03, 06, 09, 12, 15, 18, 21)에 맞아야 한다.
-- 격자에서 벗어난 시각을 넣으면 그 예약은 슬롯 개수가 맞지 않아 항상 거절된다.
ALTER TABLE `places`
    ADD COLUMN `supports_hourly` BOOLEAN NOT NULL DEFAULT TRUE
        COMMENT '시 예약 지원 여부',
    ADD COLUMN `supports_package` BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT '패키지 예약 지원 여부',
    ADD COLUMN `package_check_in_time` TIME NULL
        COMMENT '패키지 입실 시각(3시간 격자 경계)',
    ADD COLUMN `package_check_out_time` TIME NULL
        COMMENT '패키지 퇴실 시각(3시간 격자 경계)';
