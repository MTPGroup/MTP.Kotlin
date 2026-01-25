-- 优化 event_publication 表
-- 1. 为 listener_id 添加默认值
-- 2. 创建部分索引以优化未发布事件查询

-- 添加默认值
ALTER TABLE event_publication
    ALTER COLUMN listener_id SET DEFAULT 'outbox';

-- 删除旧索引
DROP INDEX IF EXISTS event_publication_by_completion_date_idx;

-- 创建部分索引（只索引未完成的事件，优化轮询查询）
CREATE INDEX idx_event_publication_unpublished
    ON event_publication (publication_date)
    WHERE completion_date IS NULL;

-- 创建已完成事件索引（用于清理任务）
CREATE INDEX idx_event_publication_completed
    ON event_publication (completion_date)
    WHERE completion_date IS NOT NULL;
