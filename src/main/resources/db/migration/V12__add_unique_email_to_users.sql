-- users.email 에 UNIQUE 제약이 없어 같은 이메일로 계정이 여러 개 만들어질 수 있었다 (#132).
--
-- AuthMapper.findByEmail 은 selectOne 이라 같은 이메일 행이 둘 이상이면
-- TooManyResultsException 이 터진다. 즉 중복 가입한 사람뿐 아니라 그 이메일을
-- 먼저 쓰던 원래 사용자까지 로그인이 막힌다.
--
-- email 은 소셜 전용 계정을 위해 NULL 을 허용한다. MySQL/MariaDB 의 UNIQUE 는
-- NULL 을 중복으로 보지 않으므로 그대로 UNIQUE 를 걸 수 있다.
-- (social_accounts 에는 처음부터 UNIQUE 가 걸려 있었고 users.email 만 빠져 있었다.)

-- 1) 이미 들어와 있는 중복을 먼저 정리한다. 정리하지 않으면 2)의 ALTER 가 실패한다.
--
--    가장 먼저 만들어진 행의 이메일을 남긴다. 그 계정이 이슈에서 말하는 "원래 사용자"라
--    이 값을 지켜야 원래 사용자의 로그인이 복구된다.
--
--    나중에 만들어진 중복 행은 이메일 뒤에 표식을 붙이고 탈퇴 처리한다. 지우지 않는
--    이유는 그 계정에 달린 반려동물·예약·리뷰가 FK 로 물려 있어서다. 값을 NULL 로
--    비우지 않는 이유는 나중에 누가 어떤 주소로 중복 가입했는지 추적할 수 없게 되기
--    때문이다. id 를 붙이므로 정리된 값끼리도 서로 겹치지 않는다.
--
--    users.email 은 VARCHAR(255) 다. 이메일이 최대 길이에 가까우면 표식을 붙였을 때
--    잘려서 서로 같은 값이 될 수 있으므로, 뒤에서 잘라내 표식이 항상 남게 한다.
UPDATE `users` u
    JOIN (
        SELECT `email`      AS email,
               MIN(`id`)    AS keep_id
        FROM `users`
        WHERE `email` IS NOT NULL
        GROUP BY `email`
        HAVING COUNT(*) > 1
    ) d ON d.`email` = u.`email` AND u.`id` <> d.`keep_id`
SET u.`email`      = RIGHT(CONCAT(u.`email`, '.dup', u.`id`), 255),
    u.`deleted_at` = COALESCE(u.`deleted_at`, CURRENT_TIMESTAMP),
    -- updated_at 은 ON UPDATE CURRENT_TIMESTAMP 라 그냥 두면 정리 작업 때문에 갱신된다.
    -- 원래 값을 다시 넣어 이력이 흐려지지 않게 한다 (V9 와 같은 방식).
    u.`updated_at` = u.`updated_at`;

-- 2) 이제 중복이 없으므로 UNIQUE 를 건다.
--    애플리케이션 쪽 사전 검사(AuthServiceImpl.signup)는 동시에 들어온 두 요청을 막지
--    못한다. 두 요청이 나란히 "없음"을 확인하고 둘 다 INSERT 하는 창이 남기 때문에,
--    마지막 방어선은 이 제약이어야 한다.
ALTER TABLE `users`
    ADD CONSTRAINT `uk_users_email` UNIQUE (`email`);
