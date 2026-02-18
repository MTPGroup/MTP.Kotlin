-- V18: 清理 messages 表

-- 1. 删除 message_type 列
alter table public.messages
    drop column if exists message_type;

-- 2. 添加索引
create index idx_messages_chat_id
    on public.messages(chat_id);

create index idx_messages_created_at
    on public.messages(created_at desc);

create index idx_messages_sender_profile_id
    on public.messages(sender_profile_id)
    where sender_profile_id is not null;

create index idx_messages_sender_character_id
    on public.messages(sender_character_id)
    where sender_character_id is not null;
