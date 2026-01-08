create table if not exists public.refresh_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(id) on delete cascade,
  token_hash text not null,
  expires_at timestamp with time zone not null,
  created_at timestamp with time zone not null default now(),
  revoked_at timestamp with time zone,
  replaced_by uuid references public.refresh_tokens(id)
);

create unique index if not exists refresh_tokens_token_hash_uq
  on public.refresh_tokens (token_hash);

create index if not exists refresh_tokens_user_id_idx
  on public.refresh_tokens (user_id);
