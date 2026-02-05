-- 重建outbox_events表
drop table if exists public.outbox_events;

create table public.outbox_events
(
    id             uuid primary key                  default gen_random_uuid(),
    aggregate_type varchar(64)              not null,
    aggregate_id   varchar(32)              not null,
    event_type     varchar(100)             not null,
    payload        jsonb                    not null,
    status         varchar(20)              not null default 'PENDING',
    retry_count    integer                  not null default 0,
    error_message  text,
    created_at     timestamp with time zone not null default now(),
    processed_at   timestamp with time zone
);

drop index if exists idx_outbox_pending;
drop index if exists idx_outbox_sent_at;

-- 添加索引
create index idx_outbox_pending on public.outbox_events (status, created_at);
create index idx_outbox_processed_at on public.outbox_events (processed_at);

-- 创建状态约束
alter table public.outbox_events
    add constraint outbox_events_status_type_check
        check ( status in ('PENDING', 'SENT', 'FAILED'))