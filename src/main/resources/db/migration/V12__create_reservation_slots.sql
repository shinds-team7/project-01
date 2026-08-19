CREATE TABLE `reservation_slots` (
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 슬롯 ID',
    `reservation_id`  BIGINT NOT NULL COMMENT '예약 ID',
    `availability_id` BIGINT NOT NULL COMMENT '슬롯 ID',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    CONSTRAINT `pk_reservation_slots` PRIMARY KEY (`id`),

    CONSTRAINT `uk_reservation_slots_availability` UNIQUE (`availability_id`),

    CONSTRAINT `fk_reservation_slots_reservation`
        FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT `fk_reservation_slots_availability`
        FOREIGN KEY (`availability_id`) REFERENCES `place_availability` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX `ix_reservation_slots_reservation` (`reservation_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='예약 점유 슬롯';