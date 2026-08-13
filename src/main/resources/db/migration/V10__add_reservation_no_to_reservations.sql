-- 예약 상세에 노출되는 '예약번호'를 담을 컬럼이 reservations 에 없었다.
-- PK 인 id 를 그대로 노출하면 전체 예약 건수가 추측되므로, 사람이 읽고 문의에 쓸 수 있는
-- 별도 식별자를 둔다. Reservation.createReservationNo() 가 'PN-yyyyMMdd-XXXXXXXX' 형식으로 발급한다.
--
-- 애플리케이션은 이미 이 컬럼을 전제로 동작한다.
-- ReservationMapper.save 가 INSERT 하고 detailReservation / findById 가 조회하므로
-- 예약 생성과 예약 상세가 모두 Unknown column 'reservation_no' 로 실패한다.

-- 1) 기존 행이 있어도 실패하지 않도록 NULL 허용으로 먼저 추가한다.
--    발급 형식이 20자로 고정돼 있으나 VARCHAR 는 실제 길이만큼만 저장하므로 여유를 둔다.
ALTER TABLE `reservations`
    ADD COLUMN `reservation_no` VARCHAR(30) NULL
        COMMENT '예약번호(PN-yyyyMMdd-XXXXXXXX)'
        AFTER `place_id`;

-- 2) 컬럼이 없던 시절에 쌓인 예약은 생성일 + id 로 채운다.
--    id 가 유일하므로 백필끼리 겹치지 않는다. 랜덤 구간은 UUID 16진수를 대문자화한 값이라
--    [0-9A-F] 만 나오므로, 16진수가 아닌 'Z' 를 앞에 두면 앞으로 발급될 번호와도 겹칠 수 없다.
--    updated_at 은 ON UPDATE CURRENT_TIMESTAMP 이므로 백필 때문에 갱신되지 않도록 원래 값을 다시 넣는다.
UPDATE `reservations`
SET `reservation_no` = CONCAT('PN-', DATE_FORMAT(`created_at`, '%Y%m%d'), '-Z', LPAD(`id`, 7, '0')),
    `updated_at`     = `updated_at`
WHERE `reservation_no` IS NULL;

-- 3) 백필로 모든 행이 값을 갖게 됐으므로 NOT NULL 로 확정한다.
ALTER TABLE `reservations`
    MODIFY COLUMN `reservation_no` VARCHAR(30) NOT NULL
        COMMENT '예약번호(PN-yyyyMMdd-XXXXXXXX)';

-- 4) 예약번호는 예약을 가리키는 외부 식별자이므로 중복될 수 없다.
--    발급 시 충돌하면 INSERT 가 실패해 같은 번호가 두 예약에 붙는 일을 막는다.
ALTER TABLE `reservations`
    ADD CONSTRAINT `uk_reservations_reservation_no`
        UNIQUE (`reservation_no`);
