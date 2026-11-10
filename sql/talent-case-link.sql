USE fdelab;

-- ============================================================
-- 人才 ↔ 标杆案例 关联迁移
-- 只加表、只加列，不删不改旧结构；可重复执行。
-- ============================================================

CREATE TABLE IF NOT EXISTS fde_talent_case (
    id          BIGINT   NOT NULL,
    talent_id   BIGINT   NOT NULL,
    case_id     BIGINT   NOT NULL,
    sort_no     INT      NOT NULL DEFAULT 0 COMMENT '详情页展示顺序，小的在前',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fde_talent_case (talent_id, case_id),
    KEY idx_fde_talent_case_talent (talent_id, sort_no),
    KEY idx_fde_talent_case_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- accepting：详情页「发起需求」是否可点
SET @db = DATABASE();
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD COLUMN accepting TINYINT NOT NULL DEFAULT 1 COMMENT ''1可接单 0暂停接单'' AFTER published',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND COLUMN_NAME = 'accepting'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- review_time：侧栏「最近复核」，NULL 则不展示该行
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD COLUMN review_time DATE NULL COMMENT ''最近复核时间'' AFTER accepting',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND COLUMN_NAME = 'review_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
