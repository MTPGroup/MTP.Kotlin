-- 移除 outbox_events 表中不再需要的聚合字段
alter table public.outbox_events
    drop column if exists aggregate_type,
    drop column if exists aggregate_id;
