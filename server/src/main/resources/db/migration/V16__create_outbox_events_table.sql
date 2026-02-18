-- 创建 outbox_events 表
create table if not exists public.outbox_events
(
    id          uuid primary key                  default gen_random_uuid(),
    type        varchar(30)              not null,
    payload     jsonb,
    status      varchar(20)              not null default 'PENDING',
    retry_count integer                  not null default 0,
    created_at  timestamp with time zone not null default now(),
    sent_at     timestamp with time zone not null default now()
);

-- 添加索引
create index idx_outbox_pending on public.outbox_events (status, created_at)
    where status = 'PENDING';
create index idx_outbox_sent_at on public.outbox_events (sent_at)
    where status = 'SENT';

-- 状态约束
alter table public.outbox_events
    add constraint outbox_events_status_type_check
        check ( status in ('PENDING', 'SENT', 'FAILED'));
