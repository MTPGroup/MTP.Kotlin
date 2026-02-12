alter table public.chat_members
    drop constraint if exists chat_members_member_ck,
    drop constraint if exists chat_members_member_type_check,
    drop constraint if exists chat_members_role_check,
    add constraint chat_members_member_ck check (
        (member_type = 'USER' and profile_id is not null and character_id is null)
            or (member_type = 'CHARACTER' and profile_id is null and character_id is not null)
        ),
    add constraint chat_members_member_type_check check (
        member_type in ('USER', 'CHARACTER')
        ),
    add constraint chat_members_role_check check (
        role in ('ADMIN', 'OWNER', 'MEMBER')
        );
