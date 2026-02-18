-- 修复contacts表的命名
alter table public.contacts
    rename column profile_id to uid;

alter table public.contacts
    rename column contact_id to character_id;
