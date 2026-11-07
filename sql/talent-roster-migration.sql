USE fdelab;

-- ============================================================
-- FDE 名录（/talents 新版）迁移脚本
-- 只加列、只加索引，不删不改旧列；可重复执行。
-- 旧数据通过 level_code IS NULL 与新版名单隔离。
-- ============================================================

-- level_code：A / P / M，等级筛选与排序，同时作为新旧数据分界
SET @db = DATABASE();
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD COLUMN level_code VARCHAR(16) NULL COMMENT ''CFDE 等级：A / P / M'' AFTER published',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND COLUMN_NAME = 'level_code'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- years：年资，按年资排序
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD COLUMN years SMALLINT NULL COMMENT ''年资（年）'' AFTER level_code',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND COLUMN_NAME = 'years'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- case_count：可公开案例数，按案例数排序
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD COLUMN case_count SMALLINT NULL DEFAULT 0 COMMENT ''可公开案例数'' AFTER years',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND COLUMN_NAME = 'case_count'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 等级筛选/排序复合索引
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD INDEX idx_fde_talent_level (published, level_code, years)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND INDEX_NAME = 'idx_fde_talent_level'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 案例数排序复合索引
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE fde_talent ADD INDEX idx_fde_talent_cases (published, case_count)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'fde_talent' AND INDEX_NAME = 'idx_fde_talent_cases'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
