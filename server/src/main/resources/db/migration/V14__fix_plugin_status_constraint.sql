-- 修复 plugins 表的 status 检查约束，改为大写以匹配 Kotlin 枚举

-- 先删除旧的约束
ALTER TABLE plugins DROP CONSTRAINT IF EXISTS plugins_status_check;

-- 添加新的约束（大写值）
ALTER TABLE plugins ADD CONSTRAINT plugins_status_check
    CHECK (status IN ('PENDING', 'REJECTED', 'APPROVED', 'ARCHIVED'));

-- 更新现有数据（如果有的话）
UPDATE plugins SET status = UPPER(status) WHERE status != UPPER(status);
