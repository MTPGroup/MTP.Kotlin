-- V20: 添加 chat_configs 表

-- 1. 创建 chat_configs 表
create table if not exists public.chat_configs (
    id uuid primary key default gen_random_uuid(),
    chat_id uuid not null unique references public.chats(id) on delete cascade,
    temperature decimal(3,2),
    max_tokens integer,
    top_p decimal(3,2),
    system_prompt text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

-- 2. 添加索引
create index if not exists idx_chat_configs_chat_id
    on public.chat_configs(chat_id);

-- 3. 添加 updated_at 触发器
create trigger chat_configs_updated_at
    before update on public.chat_configs
    for each row
    execute procedure public.update_updated_at_column();
