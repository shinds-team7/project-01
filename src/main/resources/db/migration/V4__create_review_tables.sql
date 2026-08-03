CREATE TABLE `reviews` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리뷰 ID',
    `reservation_id` BIGINT NOT NULL COMMENT '예약 ID',
    `rating` TINYINT NOT NULL COMMENT '별점',
    `content` TEXT NULL COMMENT '리뷰 내용',
    `is_read_by_host` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '호스트 확인 여부',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    `deleted_at` DATETIME NULL COMMENT '삭제 일시',

    CONSTRAINT `pk_reviews`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_reviews_reservation`
        UNIQUE (`reservation_id`),

    CONSTRAINT `fk_reviews_reservation`
        FOREIGN KEY (`reservation_id`)
        REFERENCES `reservations` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT `chk_reviews_rating`
        CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='리뷰';


CREATE TABLE `review_photos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리뷰 사진 ID',
    `review_id` BIGINT NOT NULL COMMENT '리뷰 ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '사진 정렬 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    CONSTRAINT `pk_review_photos`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_review_photos_review`
        FOREIGN KEY (`review_id`)
        REFERENCES `reviews` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='리뷰 사진';


CREATE TABLE `review_replies` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리뷰 답글 ID',
    `review_id` BIGINT NOT NULL COMMENT '리뷰 ID',
    `host_user_id` BIGINT NOT NULL COMMENT '답글 작성 호스트 ID',
    `content` TEXT NOT NULL COMMENT '답글 내용',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    `deleted_at` DATETIME NULL COMMENT '삭제 일시',

    CONSTRAINT `pk_review_replies`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_review_replies_review`
        UNIQUE (`review_id`),

    CONSTRAINT `fk_review_replies_review`
        FOREIGN KEY (`review_id`)
        REFERENCES `reviews` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT `fk_review_replies_host_user`
        FOREIGN KEY (`host_user_id`)
        REFERENCES `users` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='리뷰 답글';
