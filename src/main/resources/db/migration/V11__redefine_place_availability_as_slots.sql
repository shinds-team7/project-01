ALTER TABLE `place_availability`
    DROP COLUMN `is_available`,
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        COMMENT '슬롯 상태(OPEN/BLOCKED/RESERVED)';

ALTER TABLE `place_availability`
    ADD CONSTRAINT `uk_place_availability_slot` UNIQUE (`place_id`, `start_at`);

CREATE INDEX `ix_place_availability_lookup`
    ON `place_availability` (`place_id`, `start_at`, `status`);

ALTER TABLE `places`
    ADD COLUMN `supports_hourly` BOOLEAN NOT NULL DEFAULT TRUE
        COMMENT '시 예약 지원 여부',
    ADD COLUMN `supports_package` BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT '패키지 예약 지원 여부',
    ADD COLUMN `package_check_in_time` TIME NULL
        COMMENT '패키지 입실 시각(3시간 격자 경계)',
    ADD COLUMN `package_check_out_time` TIME NULL
        COMMENT '패키지 퇴실 시각(3시간 격자 경계)';
