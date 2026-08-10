-- #97 / PR #111 에서 places 에 호스트 닉네임을 비정규화해 저장하도록 매퍼를 바꿨으나
-- 컬럼을 추가하는 마이그레이션이 빠져 있었다.
-- HostMapper.insert 와 PlaceMapper.findAllPublished / findDetailById 가 이 컬럼을 참조하므로
-- 장소 등록·목록·상세가 모두 Unknown column 'host_user_nickname' 으로 실패한다.

-- 1) 기존 행이 있어도 실패하지 않도록 NULL 허용으로 먼저 추가한다.
ALTER TABLE `places`
    ADD COLUMN `host_user_nickname` VARCHAR(50) NULL
        COMMENT '호스트 닉네임(등록 시점 스냅샷)'
        AFTER `host_user_id`;

-- 2) 이미 등록된 장소는 현재 호스트의 닉네임으로 채운다.
--    updated_at 은 ON UPDATE CURRENT_TIMESTAMP 이므로 백필 때문에 갱신되지 않도록 원래 값을 다시 넣는다.
UPDATE `places` p
    JOIN `users` u ON u.`id` = p.`host_user_id`
SET p.`host_user_nickname` = u.`nickname`,
    p.`updated_at`         = p.`updated_at`
WHERE p.`host_user_nickname` IS NULL;

-- 3) users.nickname 이 NOT NULL 이고 places.host_user_id 가 users 를 FK 로 참조하므로
--    백필 이후에는 빈 값이 남을 수 없다. NOT NULL 로 확정한다.
ALTER TABLE `places`
    MODIFY COLUMN `host_user_nickname` VARCHAR(50) NOT NULL
        COMMENT '호스트 닉네임(등록 시점 스냅샷)';
