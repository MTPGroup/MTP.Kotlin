-- V21: 添加 chat_plugin_subscriptions 表

-- 1. 创建 chat_plugin_subscriptions 表
create table if not exists public.chat_plugin_subscriptions (
    id uuid primary key default gen_random_uuid(),
    chat_id uuid not null references public.chats(id) on delete cascade,
    plugin_id uuid not null references public.plugins(id) on delete cascade,
    enabled boolean not null default true,
    config jsonb not null default '{}'::jsonb,
    created_at timestamp with time zone not null default now(),
    constraint chat_plugin_subscriptions_unique unique (chat_id, plugin_id)
);

-- 2. 添加索引
create index if not exists idx_chat_plugin_subscriptions_chat_id
    on public.chat_plugin_subscriptions(chat_id);

create index if not exists idx_chat_plugin_subscriptions_plugin_id
    on public.chat_plugin_subscriptions(plugin_id);

create index if not exists idx_chat_plugin_subscriptions_enabled
    on public.chat_plugin_subscriptions(enabled)
    where enabled = true;
