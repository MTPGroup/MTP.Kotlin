-- create email_opts and idx
create table if not exists public.email_otps (
    id uuid primary key default gen_random_uuid(),
    email text not null,
    type varchar(30) not null,
    code_hash text not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null default now(),
    used_at timestamp with time zone
                             );

create index if not exists email_otps_email_type_created_at_idx
    on public.email_otps (email, type, created_at desc);

-- remove user creation trigger, handled by application layer
drop trigger if exists on_user_created on public.users;
drop function if exists public.handle_new_user();
