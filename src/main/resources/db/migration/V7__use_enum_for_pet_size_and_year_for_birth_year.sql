UPDATE `pets`
SET `size` = CASE BINARY LOWER(TRIM(`size`))
    WHEN 'small' THEN 'SMALL'
    WHEN '소형' THEN 'SMALL'
    WHEN 'medium' THEN 'MEDIUM'
    WHEN '중형' THEN 'MEDIUM'
    WHEN 'large' THEN 'LARGE'
    WHEN '대형' THEN 'LARGE'
    ELSE `size`
END,
    `updated_at` = `updated_at`
WHERE `size` IS NOT NULL;

ALTER TABLE `pets`
    MODIFY COLUMN `size`
        ENUM('SMALL', 'MEDIUM', 'LARGE') NULL
        COMMENT '크기 구분',
    MODIFY COLUMN `birth_year`
        YEAR NULL
        COMMENT '출생 연도';
