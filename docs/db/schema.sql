-- =====================================================================
-- petNow 통합 DDL 스냅샷 (MariaDB 11.x)
-- 실제 애플리케이션 스키마 변경은 src/main/resources/db/migration의
-- Flyway 마이그레이션을 기준으로 관리한다.
--
-- 명명 규칙
--   1) 모든 테이블의 자체 PK는 id로 통일한다.
--   2) FK는 참조 대상에 따라 {대상}_id 형식으로 작성한다.
--   3) place_type은 PlaceType enum(APARTMENT, HOUSE, OFFICETEL)과 동기화한다.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS petnow
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE petnow;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS review_photos;
DROP TABLE IF EXISTS review_replies;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS reservation_pets;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS place_availability;
DROP TABLE IF EXISTS place_addresses;
DROP TABLE IF EXISTS place_photos;
DROP TABLE IF EXISTS places;
DROP TABLE IF EXISTS pet_photos;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS social_accounts;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 사용자
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    email             VARCHAR(255) NULL,
    password          VARCHAR(255) NULL COMMENT 'BCrypt 해시. 소셜 전용 계정은 NULL',
    nickname          VARCHAR(50)  NOT NULL,
    phone             VARCHAR(20)  NULL,
    profile_image_url VARCHAR(500) NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME     NULL,
    CONSTRAINT PK_USERS PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 소셜 계정
-- ---------------------------------------------------------------------
CREATE TABLE social_accounts (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    provider          VARCHAR(20)  NOT NULL,
    provider_user_id  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255) NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SOCIAL_ACCOUNTS PRIMARY KEY (id),
    CONSTRAINT UK_SOCIAL_ACCOUNTS_PROVIDER UNIQUE (provider, provider_user_id),
    CONSTRAINT UK_SOCIAL_ACCOUNTS_USER_PROVIDER UNIQUE (user_id, provider),
    CONSTRAINT FK_SOCIAL_ACCOUNTS_USER FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 반려동물
-- ---------------------------------------------------------------------
CREATE TABLE pets (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    name        VARCHAR(20)   NOT NULL,
    weight      DECIMAL(5, 2) NULL,
    breed       VARCHAR(50)   NULL,
    sex         VARCHAR(10)   NULL,
    is_neutered BOOLEAN       NOT NULL DEFAULT FALSE,
    size        VARCHAR(10)   NULL,
    birth_year  SMALLINT      NULL,
    memo        VARCHAR(500)  NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  DATETIME      NULL,
    CONSTRAINT PK_PETS PRIMARY KEY (id),
    CONSTRAINT FK_PETS_USER FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE pet_photos (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    pet_id       BIGINT       NOT NULL,
    image_url    VARCHAR(500) NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_PET_PHOTOS PRIMARY KEY (id),
    CONSTRAINT FK_PET_PHOTOS_PET FOREIGN KEY (pet_id) REFERENCES pets (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 장소
-- ---------------------------------------------------------------------
CREATE TABLE places (
    id                      BIGINT         NOT NULL AUTO_INCREMENT,
    host_user_id            BIGINT         NOT NULL,
    name                    VARCHAR(100)   NOT NULL,
    description             TEXT           NULL,
    place_type              ENUM('APARTMENT', 'HOUSE', 'OFFICETEL') NULL,
    area_size               DECIMAL(8, 2)  NULL,
    capacity                SMALLINT       NOT NULL,
    allows_small_dog        BOOLEAN        NOT NULL DEFAULT FALSE,
    allows_medium_dog       BOOLEAN        NOT NULL DEFAULT FALSE,
    allows_large_dog        BOOLEAN        NOT NULL DEFAULT FALSE,
    provides_home_camera    BOOLEAN        NOT NULL DEFAULT FALSE,
    provides_realtime_photo BOOLEAN        NOT NULL DEFAULT FALSE,
    provides_yard           BOOLEAN        NOT NULL DEFAULT FALSE,
    provides_walk           BOOLEAN        NOT NULL DEFAULT FALSE,
    other_options           VARCHAR(1000)  NULL,
    hourly_price            DECIMAL(12, 0) NULL,
    nightly_price           DECIMAL(12, 0) NULL,
    status                  VARCHAR(20)    NOT NULL,
    is_visible              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at              DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at              DATETIME       NULL,
    CONSTRAINT PK_PLACES PRIMARY KEY (id),
    CONSTRAINT FK_PLACES_HOST FOREIGN KEY (host_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE place_photos (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    place_id       BIGINT       NOT NULL,
    image_url      VARCHAR(500) NOT NULL,
    sort_order     INT          NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_PLACE_PHOTOS PRIMARY KEY (id),
    CONSTRAINT FK_PLACE_PHOTOS_PLACE FOREIGN KEY (place_id) REFERENCES places (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE place_addresses (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    place_id         BIGINT         NOT NULL,
    address_code     VARCHAR(30)    NULL,
    sido             VARCHAR(30)    NOT NULL,
    sigungu          VARCHAR(50)    NOT NULL,
    eupmyeondong     VARCHAR(50)    NULL,
    road_address     VARCHAR(255)   NOT NULL,
    detail_address   VARCHAR(255)   NULL,
    postal_code      VARCHAR(10)    NULL,
    latitude         DECIMAL(10, 7) NULL,
    longitude        DECIMAL(10, 7) NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_PLACE_ADDRESSES PRIMARY KEY (id),
    CONSTRAINT UK_PLACE_ADDRESSES_PLACE UNIQUE (place_id),
    CONSTRAINT FK_PLACE_ADDRESSES_PLACE FOREIGN KEY (place_id) REFERENCES places (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE place_availability (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    place_id        BIGINT   NOT NULL,
    start_at        DATETIME NOT NULL,
    end_at          DATETIME NOT NULL,
    is_available    BOOLEAN  NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_PLACE_AVAILABILITY PRIMARY KEY (id),
    CONSTRAINT FK_PLACE_AVAILABILITY_PLACE FOREIGN KEY (place_id) REFERENCES places (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE bookmarks (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    user_id     BIGINT   NOT NULL,
    place_id    BIGINT   NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_BOOKMARKS PRIMARY KEY (id),
    CONSTRAINT UK_BOOKMARKS_USER_PLACE UNIQUE (user_id, place_id),
    CONSTRAINT FK_BOOKMARKS_USER FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_BOOKMARKS_PLACE FOREIGN KEY (place_id) REFERENCES places (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 예약
-- ---------------------------------------------------------------------
CREATE TABLE reservations (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    guest_user_id    BIGINT         NOT NULL,
    place_id         BIGINT         NOT NULL,
    reservation_type VARCHAR(20)    NOT NULL,
    check_in_at      DATETIME       NOT NULL,
    check_out_at     DATETIME       NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    request_message  VARCHAR(500)   NULL,
    total_price      DECIMAL(12, 0) NOT NULL,
    responded_at     DATETIME       NULL,
    canceled_at      DATETIME       NULL,
    cancel_reason    VARCHAR(500)   NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_RESERVATIONS PRIMARY KEY (id),
    CONSTRAINT FK_RESERVATIONS_GUEST FOREIGN KEY (guest_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT FK_RESERVATIONS_PLACE FOREIGN KEY (place_id) REFERENCES places (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE reservation_pets (
    id                 BIGINT   NOT NULL AUTO_INCREMENT,
    reservation_id     BIGINT   NOT NULL,
    pet_id             BIGINT   NOT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_RESERVATION_PETS PRIMARY KEY (id),
    CONSTRAINT UK_RESERVATION_PETS UNIQUE (reservation_id, pet_id),
    CONSTRAINT FK_RESERVATION_PETS_RESERVATION FOREIGN KEY (reservation_id) REFERENCES reservations (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_RESERVATION_PETS_PET FOREIGN KEY (pet_id) REFERENCES pets (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 결제
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    reservation_id  BIGINT         NOT NULL,
    provider        VARCHAR(30)    NOT NULL,
    payment_method  VARCHAR(30)    NULL,
    order_id        VARCHAR(100)   NOT NULL,
    payment_key     VARCHAR(255)   NULL,
    amount          DECIMAL(12, 0) NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    failure_code    VARCHAR(100)   NULL,
    failure_message VARCHAR(500)   NULL,
    approved_at     DATETIME       NULL,
    failed_at       DATETIME       NULL,
    canceled_at     DATETIME       NULL,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_PAYMENTS PRIMARY KEY (id),
    CONSTRAINT UK_PAYMENTS_ORDER_ID UNIQUE (order_id),
    CONSTRAINT UK_PAYMENTS_PAYMENT_KEY UNIQUE (payment_key),
    CONSTRAINT FK_PAYMENTS_RESERVATION FOREIGN KEY (reservation_id) REFERENCES reservations (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 리뷰
-- ---------------------------------------------------------------------
CREATE TABLE reviews (
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    reservation_id   BIGINT   NOT NULL,
    rating           TINYINT  NOT NULL,
    content          TEXT     NULL,
    is_read_by_host  BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at       DATETIME NULL,
    CONSTRAINT PK_REVIEWS PRIMARY KEY (id),
    CONSTRAINT UK_REVIEWS_RESERVATION UNIQUE (reservation_id),
    CONSTRAINT FK_REVIEWS_RESERVATION FOREIGN KEY (reservation_id) REFERENCES reservations (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT CK_REVIEWS_RATING CHECK (rating BETWEEN 1 AND 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE review_photos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    review_id       BIGINT       NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_REVIEW_PHOTOS PRIMARY KEY (id),
    CONSTRAINT FK_REVIEW_PHOTOS_REVIEW FOREIGN KEY (review_id) REFERENCES reviews (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE review_replies (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    review_id       BIGINT   NOT NULL,
    host_user_id    BIGINT   NOT NULL,
    content         TEXT     NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      DATETIME NULL,
    CONSTRAINT PK_REVIEW_REPLIES PRIMARY KEY (id),
    CONSTRAINT UK_REVIEW_REPLIES_REVIEW UNIQUE (review_id),
    CONSTRAINT FK_REVIEW_REPLIES_REVIEW FOREIGN KEY (review_id) REFERENCES reviews (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_REVIEW_REPLIES_HOST FOREIGN KEY (host_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
