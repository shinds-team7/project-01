ALTER TABLE `users`
    ADD COLUMN `password` VARCHAR(255) NULL COMMENT '비밀번호(해시)' AFTER `nickname`;
