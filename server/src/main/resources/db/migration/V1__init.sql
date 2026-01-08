-- azusa core schema for ktor + exposed + postgresql
-- includes tables, indexes, functions, and triggers needed by the api

create schema if not exists extensions;

create extension if not exists "uuid-ossp" with schema extensions;
create extension if not exists "pgcrypto" with schema extensions;
create extension if not exists vector with schema extensions;

-- users (replacement for supabase auth.users)
create table if not exists public.users (
  id uuid primary key default gen_random_uuid(),
  email text unique,
  password_hash text not null,
  raw_user_meta_data jsonb not null default '{}'::jsonb,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

-- profiles and settings
create table if not exists public.profiles (
  id uuid primary key default gen_random_uuid(),
  uid uuid unique references public.users(id) on delete cascade not null,
  username varchar(50) not null,
  avatar text,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.settings (
  owner_id uuid primary key references public.profiles(id) on delete cascade,
  theme varchar(30) not null default 'system',
  chat_models jsonb not null default '[]'::jsonb,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

-- plugins
create table if not exists public.plugins (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  description text not null,
  version text not null,
  liked integer not null default 0,
  status varchar(20) not null default 'pending'
    check (status in ('pending', 'rejected', 'approved', 'archived')),
  schema jsonb not null,
  code text not null,
  author_id uuid references public.profiles(id) on delete cascade not null,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.plugin_subscriptions (
  user_id uuid not null references public.profiles(id) on delete cascade,
  plugin_id uuid not null references public.plugins(id) on delete cascade,
  is_active boolean not null default false,
  subscribed_at timestamp with time zone not null default now(),
  primary key (user_id, plugin_id)
);

create table if not exists public.plugin_likes (
  user_id uuid not null references public.profiles(id) on delete cascade,
  plugin_id uuid not null references public.plugins(id) on delete cascade,
  created_at timestamp with time zone not null default now(),
  primary key (user_id, plugin_id)
);

-- characters and contacts
create table if not exists public.characters (
  id uuid primary key default gen_random_uuid(),
  author_id uuid references public.profiles(id) on delete cascade not null,
  name varchar(100) not null,
  avatar text,
  bio text,
  origin_prompt text,
  is_public boolean not null default false,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.contacts (
  profile_id uuid not null references public.profiles(id) on delete cascade,
  contact_id uuid not null references public.characters(id) on delete cascade,
  nickname varchar(100),
  added_at timestamp with time zone not null default now(),
  primary key (profile_id, contact_id)
);

-- knowledge base
create table if not exists public.knowledge_bases (
  id uuid primary key default gen_random_uuid(),
  name varchar(100) not null,
  description text,
  author_id uuid references public.profiles(id) on delete cascade not null,
  is_public boolean not null default false,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.knowledge_files (
  id uuid primary key default gen_random_uuid(),
  knowledge_base_id uuid references public.knowledge_bases(id) on delete cascade not null,
  file_path text not null,
  file_name text not null,
  file_size integer,
  file_type text,
  status varchar(20) not null default 'pending'
    check (status in ('pending', 'processing', 'completed', 'failed')),
  error_message text,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.knowledge_documents (
  id uuid primary key default gen_random_uuid(),
  knowledge_base_id uuid references public.knowledge_bases(id) on delete cascade not null,
  file_id uuid references public.knowledge_files(id) on delete cascade,
  content text not null,
  metadata jsonb not null default '{}'::jsonb,
  embedding extensions.vector(1024),
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.knowledge_subscriptions (
  character_id uuid not null references public.characters(id) on delete cascade,
  knowledge_base_id uuid not null references public.knowledge_bases(id) on delete cascade,
  priority integer not null default 0,
  primary key (character_id, knowledge_base_id)
);

-- chats and messages
create table if not exists public.chats (
  id uuid primary key default gen_random_uuid(),
  name text,
  avatar text,
  last_message text,
  is_group boolean not null default false,
  owner_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);

create table if not exists public.chat_members (
  id uuid primary key default gen_random_uuid(),
  chat_id uuid not null references public.chats(id) on delete cascade,
  member_type varchar(20) not null check (member_type in ('user', 'character')),
  profile_id uuid references public.profiles(id) on delete cascade,
  character_id uuid references public.characters(id) on delete cascade,
  role varchar(20) not null default 'member' check (role in ('owner', 'admin', 'member')),
  joined_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
  constraint chat_members_member_ck check (
    (member_type = 'user' and profile_id is not null and character_id is null)
    or (member_type = 'character' and character_id is not null and profile_id is null)
  )
);

create unique index if not exists chat_members_user_unique
  on public.chat_members(chat_id, profile_id)
  where member_type = 'user';

create unique index if not exists chat_members_character_unique
  on public.chat_members(chat_id, character_id)
  where member_type = 'character';

create table if not exists public.messages (
  id uuid primary key default gen_random_uuid(),
  chat_id uuid not null references public.chats(id) on delete cascade,
  sender_type varchar(20) not null check (sender_type in ('user', 'character')),
  sender_profile_id uuid references public.profiles(id) on delete cascade,
  sender_character_id uuid references public.characters(id) on delete cascade,
  message_type varchar(20) not null default 'text',
  content jsonb default '[]'::jsonb,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now(),
  constraint messages_sender_ck check (
    (sender_type = 'user' and sender_profile_id is not null and sender_character_id is null)
    or (sender_type = 'character' and sender_character_id is not null and sender_profile_id is null)
  )
);

-- updated_at trigger function
create or replace function public.update_updated_at_column()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

-- user creation hook (replacement for supabase auth trigger)
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  new_profile_id uuid;
  user_username text;
  user_avatar text;
begin
  user_username := new.raw_user_meta_data->>'username';
  if user_username is null or user_username = '' then
    user_username := 'User_' || substr(new.id::text, 1, 8);
  end if;

  user_avatar := new.raw_user_meta_data->>'avatar_url';

  insert into public.profiles (uid, username, avatar)
  values (new.id, user_username, user_avatar)
  returning id into new_profile_id;

  insert into public.settings (owner_id)
  values (new_profile_id);

  return new;
end;
$$;

drop trigger if exists on_user_created on public.users;
create trigger on_user_created
  after insert on public.users
  for each row execute function public.handle_new_user();

-- knowledge base link/unlink
create or replace function public.link_knowledge_base_to_ai(
  p_character_id uuid,
  p_knowledge_base_id uuid,
  p_priority integer default 0
) returns void
language plpgsql
set search_path = ''
as $$
begin
  insert into public.knowledge_subscriptions (character_id, knowledge_base_id, priority)
  values (p_character_id, p_knowledge_base_id, coalesce(p_priority, 0))
  on conflict (character_id, knowledge_base_id)
  do update set priority = excluded.priority;
end;
$$;

create or replace function public.unlink_knowledge_base_from_ai(
  p_character_id uuid,
  p_knowledge_base_id uuid
) returns void
language plpgsql
set search_path = ''
as $$
begin
  delete from public.knowledge_subscriptions
  where character_id = p_character_id
    and knowledge_base_id = p_knowledge_base_id;
end;
$$;

-- vector search
create or replace function public.match_knowledge_documents(
  query_embedding extensions.vector(1024),
  knowledge_base_ids uuid[],
  match_threshold float default 0.5,
  match_count int default 10
)
returns table (
  id uuid,
  knowledge_base_id uuid,
  content text,
  metadata jsonb,
  similarity float
)
language plpgsql
set search_path = public, extensions
as $$
begin
  return query
  select
    kd.id,
    kd.knowledge_base_id,
    kd.content,
    kd.metadata,
    1 - (kd.embedding <=> query_embedding) as similarity
  from public.knowledge_documents kd
  where
    kd.knowledge_base_id = any(knowledge_base_ids)
    and 1 - (kd.embedding <=> query_embedding) > match_threshold
  order by kd.embedding <=> query_embedding
  limit match_count;
end;
$$;

-- plugin like counter
create or replace function public.update_plugin_like_count()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if (tg_op = 'insert') then
    update public.plugins
    set liked = liked + 1
    where id = new.plugin_id;
  elsif (tg_op = 'delete') then
    update public.plugins
    set liked = liked - 1
    where id = old.plugin_id;
  end if;
  return null;
end;
$$;

drop trigger if exists on_plugin_like_change on public.plugin_likes;
create trigger on_plugin_like_change
after insert or delete on public.plugin_likes
for each row execute function public.update_plugin_like_count();

-- updated_at triggers
drop trigger if exists update_users_updated_at on public.users;
create trigger update_users_updated_at before update on public.users
for each row execute function public.update_updated_at_column();

drop trigger if exists update_profiles_updated_at on public.profiles;
create trigger update_profiles_updated_at before update on public.profiles
for each row execute function public.update_updated_at_column();

drop trigger if exists update_settings_updated_at on public.settings;
create trigger update_settings_updated_at before update on public.settings
for each row execute function public.update_updated_at_column();

drop trigger if exists update_plugins_updated_at on public.plugins;
create trigger update_plugins_updated_at before update on public.plugins
for each row execute function public.update_updated_at_column();

drop trigger if exists update_characters_updated_at on public.characters;
create trigger update_characters_updated_at before update on public.characters
for each row execute function public.update_updated_at_column();

drop trigger if exists update_knowledge_bases_updated_at on public.knowledge_bases;
create trigger update_knowledge_bases_updated_at before update on public.knowledge_bases
for each row execute function public.update_updated_at_column();

drop trigger if exists update_knowledge_files_updated_at on public.knowledge_files;
create trigger update_knowledge_files_updated_at before update on public.knowledge_files
for each row execute function public.update_updated_at_column();

drop trigger if exists update_knowledge_documents_updated_at on public.knowledge_documents;
create trigger update_knowledge_documents_updated_at before update on public.knowledge_documents
for each row execute function public.update_updated_at_column();

drop trigger if exists update_chats_updated_at on public.chats;
create trigger update_chats_updated_at before update on public.chats
for each row execute function public.update_updated_at_column();

drop trigger if exists update_chat_members_updated_at on public.chat_members;
create trigger update_chat_members_updated_at before update on public.chat_members
for each row execute function public.update_updated_at_column();

drop trigger if exists update_messages_updated_at on public.messages;
create trigger update_messages_updated_at before update on public.messages
for each row execute function public.update_updated_at_column();
