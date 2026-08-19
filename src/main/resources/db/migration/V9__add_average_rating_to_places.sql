-- 별점은 reviews 에만 있고 reviews -> reservations -> places 로 조인해야 장소별 평균이 나온다.
-- 목록·상세를 조회할 때마다 집계하는 대신, host_user_nickname 과 같이 places 에 비정규화해 두고
-- 리뷰가 등록·수정·삭제될 때 갱신한다.

-- 1) 기존 행이 있어도 실패하지 않도록 기본값 0.00 으로 추가한다.
--    rating 은 1~5 이므로 평균은 최대 5.00 이고, 소수 둘째 자리까지 두면 반올림 오차가 눈에 띄지 않는다.
ALTER TABLE `places`
    ADD COLUMN `average_rating` DECIMAL(3, 2) NOT NULL DEFAULT 0.00
        COMMENT '리뷰 평균 별점(리뷰 없으면 0.00)'
        AFTER `nightly_price`;

-- 2) 이미 등록된 장소는 현재 리뷰들의 평균으로 채운다. 삭제된 리뷰는 제외한다.
--    updated_at 은 ON UPDATE CURRENT_TIMESTAMP 이므로 백필 때문에 갱신되지 않도록 원래 값을 다시 넣는다.
UPDATE `places` p
    JOIN (
        SELECT rs.`place_id`         AS place_id,
               AVG(rv.`rating`)      AS average_rating
        FROM `reviews` rv
            JOIN `reservations` rs ON rs.`id` = rv.`reservation_id`
        WHERE rv.`deleted_at` IS NULL
        GROUP BY rs.`place_id`
    ) r ON r.`place_id` = p.`id`
SET p.`average_rating` = r.`average_rating`,
    p.`updated_at`     = p.`updated_at`;
