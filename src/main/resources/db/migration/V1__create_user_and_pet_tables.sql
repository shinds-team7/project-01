CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
    `email` VARCHAR(255) NULL COMMENT '이메일',
    `nickname` VARCHAR(50) NOT NULL COMMENT '닉네임',
    `phone` VARCHAR(20) NULL COMMENT '전화번호',
    `profile_image_url` VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    `deleted_at` DATETIME NULL COMMENT '삭제 일시',

    CONSTRAINT `pk_users`
        PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='회원';


CREATE TABLE `social_accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '소셜 계정 ID',
    `user_id` BIGINT NOT NULL COMMENT '회원 ID',
    `provider` VARCHAR(20) NOT NULL COMMENT '소셜 로그인 제공자',
    `provider_user_id` VARCHAR(255) NOT NULL COMMENT '제공자 회원 식별자',
    `provider_email` VARCHAR(255) NULL COMMENT '제공자 이메일',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_social_accounts`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_social_accounts_provider_user`
        UNIQUE (`provider`, `provider_user_id`),

    CONSTRAINT `uk_social_accounts_user_provider`
        UNIQUE (`user_id`, `provider`),

    CONSTRAINT `fk_social_accounts_user`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='소셜 계정';


CREATE TABLE `pets` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '반려동물 ID',
    `user_id` BIGINT NOT NULL COMMENT '보호자 회원 ID',
    `name` VARCHAR(20) NOT NULL COMMENT '반려동물 이름',
    `weight` DECIMAL(5, 2) NULL COMMENT '몸무게',
    `breed` VARCHAR(50) NULL COMMENT '품종',
    `sex` VARCHAR(10) NULL COMMENT '성별',
    `is_neutered` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '중성화 여부',
    `size` VARCHAR(10) NULL COMMENT '크기 구분',
    `birth_year` SMALLINT NULL COMMENT '출생 연도',
    `memo` VARCHAR(500) NULL COMMENT '특이사항',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    `deleted_at` DATETIME NULL COMMENT '삭제 일시',

    CONSTRAINT `pk_pets`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_pets_user`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='반려동물';


CREATE TABLE `pet_photos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '반려동물 사진 ID',
    `pet_id` BIGINT NOT NULL COMMENT '반려동물 ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '사진 정렬 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_pet_photos`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_pet_photos_pet`
        FOREIGN KEY (`pet_id`)
        REFERENCES `pets` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='반려동물 사진';
