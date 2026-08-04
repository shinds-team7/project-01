CREATE TABLE `reservations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 ID',
    `guest_user_id` BIGINT NOT NULL COMMENT '게스트 회원 ID',
    `place_id` BIGINT NOT NULL COMMENT '장소 ID',
    `reservation_type` VARCHAR(20) NOT NULL COMMENT '예약 유형',
    `check_in_at` DATETIME NOT NULL COMMENT '예약 시작 일시',
    `check_out_at` DATETIME NOT NULL COMMENT '예약 종료 일시',
    `status` VARCHAR(20) NOT NULL COMMENT '예약 상태',
    `request_message` VARCHAR(500) NULL COMMENT '예약 요청 메시지',
    `total_price` DECIMAL(12, 0) NOT NULL COMMENT '총 예약 금액',
    `responded_at` DATETIME NULL COMMENT '호스트 응답 일시',
    `canceled_at` DATETIME NULL COMMENT '예약 취소 일시',
    `cancel_reason` VARCHAR(500) NULL COMMENT '예약 취소 사유',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_reservations`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_reservations_guest_user`
        FOREIGN KEY (`guest_user_id`)
        REFERENCES `users` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT `fk_reservations_place`
        FOREIGN KEY (`place_id`)
        REFERENCES `places` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='예약';


CREATE TABLE `reservation_pets` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 반려동물 ID',
    `reservation_id` BIGINT NOT NULL COMMENT '예약 ID',
    `pet_id` BIGINT NOT NULL COMMENT '반려동물 ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',

    CONSTRAINT `pk_reservation_pets`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_reservation_pets_reservation_pet`
        UNIQUE (`reservation_id`, `pet_id`),

    CONSTRAINT `fk_reservation_pets_reservation`
        FOREIGN KEY (`reservation_id`)
        REFERENCES `reservations` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT `fk_reservation_pets_pet`
        FOREIGN KEY (`pet_id`)
        REFERENCES `pets` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='예약 반려동물';


CREATE TABLE `payments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결제 ID',
    `reservation_id` BIGINT NOT NULL COMMENT '예약 ID',
    `provider` VARCHAR(30) NOT NULL COMMENT '결제 제공사',
    `payment_method` VARCHAR(30) NULL COMMENT '결제 수단',
    `order_id` VARCHAR(100) NOT NULL COMMENT '주문 ID',
    `payment_key` VARCHAR(255) NULL COMMENT '결제 키',
    `amount` DECIMAL(12, 0) NOT NULL COMMENT '결제 금액',
    `status` VARCHAR(20) NOT NULL COMMENT '결제 상태',
    `failure_code` VARCHAR(100) NULL COMMENT '결제 실패 코드',
    `failure_message` VARCHAR(500) NULL COMMENT '결제 실패 메시지',
    `approved_at` DATETIME NULL COMMENT '결제 승인 일시',
    `failed_at` DATETIME NULL COMMENT '결제 실패 일시',
    `canceled_at` DATETIME NULL COMMENT '결제 취소 일시',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_payments`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_payments_order_id`
        UNIQUE (`order_id`),

    CONSTRAINT `uk_payments_payment_key`
        UNIQUE (`payment_key`),

    CONSTRAINT `fk_payments_reservation`
        FOREIGN KEY (`reservation_id`)
        REFERENCES `reservations` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='결제';
