ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS status         text,
    ADD COLUMN IF NOT EXISTS email_verified boolean,
    ADD COLUMN IF NOT EXISTS banned_until   timestamptz;

UPDATE public.users
SET status         = COALESCE(raw_user_meta_data ->> 'status', 'pending'),
    email_verified = COALESCE((raw_user_meta_data ->> 'emailVerified')::boolean, false),
    banned_until   = NULLIF(raw_user_meta_data ->> 'bannedUntil', '')::timestamptz
WHERE status IS NULL
   OR email_verified IS NULL
   OR banned_until IS NULL;

ALTER TABLE public.users
    ALTER COLUMN status SET DEFAULT 'pending',
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN email_verified SET DEFAULT false,
    ALTER COLUMN email_verified SET NOT NULL;

ALTER TABLE public.users
    DROP COLUMN IF EXISTS raw_user_meta_data;

ALTER TABLE public.email_otps
    ADD COLUMN IF NOT EXISTS is_used boolean;