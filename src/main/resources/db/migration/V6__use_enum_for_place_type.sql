ALTER TABLE `places`
    MODIFY COLUMN `place_type`
        ENUM('APARTMENT', 'HOUSE', 'OFFICETEL') NULL
        COMMENT '장소 유형';
