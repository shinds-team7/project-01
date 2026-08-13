-- 예약이 어떤 슬롯을 점유했는지 기록한다.
--
-- 취소 시 어떤 슬롯을 OPEN 으로 되돌려야 하는지 알려면 이 매핑이 필요하다.
-- check_in_at / check_out_at 으로 역산할 수도 있지만, 그 사이 호스트가 슬롯을
-- 지우거나 바꿨을 때 되돌릴 대상이 어긋난다.
CREATE TABLE `reservation_slots` (
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 슬롯 ID',
    `reservation_id`  BIGINT NOT NULL COMMENT '예약 ID',
    `availability_id` BIGINT NOT NULL COMMENT '슬롯 ID',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    CONSTRAINT `pk_reservation_slots`
        PRIMARY KEY (`id`),

    -- 슬롯 하나는 예약 하나에만 붙는다.
    -- 애플리케이션 로직에 구멍이 나도 더블부킹이 DB 차원에서 물리적으로 불가능해진다.
    -- 취소 시에는 이 매핑을 DELETE 하므로 해당 슬롯을 다시 예약할 수 있다.
    CONSTRAINT `uk_reservation_slots_availability`
        UNIQUE (`availability_id`),

    CONSTRAINT `fk_reservation_slots_reservation`
        FOREIGN KEY (`reservation_id`)
        REFERENCES `reservations` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT `fk_reservation_slots_availability`
        FOREIGN KEY (`availability_id`)
        REFERENCES `place_availability` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX `ix_reservation_slots_reservation` (`reservation_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='예약 점유 슬롯';
