CREATE TABLE `places` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '장소 ID',
    `host_user_id` BIGINT NOT NULL COMMENT '호스트 회원 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '장소명',
    `description` TEXT NULL COMMENT '장소 설명',
    `place_type` VARCHAR(30) NULL COMMENT '장소 유형',
    `area_size` DECIMAL(8, 2) NULL COMMENT '장소 면적',
    `capacity` SMALLINT NOT NULL COMMENT '최대 수용 마릿수',
    `allows_small_dog` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '소형견 허용 여부',
    `allows_medium_dog` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '중형견 허용 여부',
    `allows_large_dog` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '대형견 허용 여부',
    `provides_home_camera` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '홈 카메라 제공 여부',
    `provides_realtime_photo` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '실시간 사진 제공 여부',
    `provides_yard` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '마당 제공 여부',
    `provides_walk` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '산책 제공 여부',
    `other_options` VARCHAR(1000) NULL COMMENT '기타 제공 옵션',
    `hourly_price` DECIMAL(12, 0) NULL COMMENT '시간당 가격',
    `nightly_price` DECIMAL(12, 0) NULL COMMENT '숙박 가격',
    `status` VARCHAR(20) NOT NULL COMMENT '장소 상태',
    `is_visible` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '게시글 공개 여부',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    `deleted_at` DATETIME NULL COMMENT '삭제 일시',

    CONSTRAINT `pk_places`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_places_host_user`
        FOREIGN KEY (`host_user_id`)
        REFERENCES `users` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장소';


CREATE TABLE `place_addresses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '장소 주소 ID',
    `place_id` BIGINT NOT NULL COMMENT '장소 ID',
    `address_code` VARCHAR(30) NULL COMMENT '법정동 또는 행정구역 코드',
    `sido` VARCHAR(30) NOT NULL COMMENT '시도',
    `sigungu` VARCHAR(50) NOT NULL COMMENT '시군구',
    `eupmyeondong` VARCHAR(50) NULL COMMENT '읍면동',
    `road_address` VARCHAR(255) NOT NULL COMMENT '도로명 주소',
    `detail_address` VARCHAR(255) NULL COMMENT '상세 주소',
    `postal_code` VARCHAR(10) NULL COMMENT '우편번호',
    `latitude` DECIMAL(10, 7) NULL COMMENT '위도',
    `longitude` DECIMAL(10, 7) NULL COMMENT '경도',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_place_addresses`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_place_addresses_place`
        UNIQUE (`place_id`),

    CONSTRAINT `fk_place_addresses_place`
        FOREIGN KEY (`place_id`)
        REFERENCES `places` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장소 주소';


CREATE TABLE `place_photos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '장소 사진 ID',
    `place_id` BIGINT NOT NULL COMMENT '장소 ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '이미지 URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '사진 정렬 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_place_photos`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_place_photos_place`
        FOREIGN KEY (`place_id`)
        REFERENCES `places` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장소 사진';


CREATE TABLE `place_availability` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 가능 시간 ID',
    `place_id` BIGINT NOT NULL COMMENT '장소 ID',
    `start_at` DATETIME NOT NULL COMMENT '시작 일시',
    `end_at` DATETIME NOT NULL COMMENT '종료 일시',
    `is_available` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '예약 가능 여부',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    CONSTRAINT `pk_place_availability`
        PRIMARY KEY (`id`),

    CONSTRAINT `fk_place_availability_place`
        FOREIGN KEY (`place_id`)
        REFERENCES `places` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장소 예약 가능 시간';


CREATE TABLE `bookmarks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '북마크 ID',
    `user_id` BIGINT NOT NULL COMMENT '회원 ID',
    `place_id` BIGINT NOT NULL COMMENT '장소 ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',

    CONSTRAINT `pk_bookmarks`
        PRIMARY KEY (`id`),

    CONSTRAINT `uk_bookmarks_user_place`
        UNIQUE (`user_id`, `place_id`),

    CONSTRAINT `fk_bookmarks_user`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT `fk_bookmarks_place`
        FOREIGN KEY (`place_id`)
        REFERENCES `places` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='북마크';
