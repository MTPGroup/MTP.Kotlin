UPDATE public.settings
SET owner_id = p.uid
FROM public.profiles p
WHERE settings.owner_id = p.id;

UPDATE public.plugins
SET author_id = p.uid
FROM public.profiles p
WHERE plugins.author_id = p.id;

UPDATE public.plugin_subscriptions
SET user_id = p.uid
FROM public.profiles p
WHERE plugin_subscriptions.user_id = p.id;

UPDATE public.plugin_likes
SET user_id = p.uid
FROM public.profiles p
WHERE plugin_likes.user_id = p.id;

UPDATE public.characters
SET author_id = p.uid
FROM public.profiles p
WHERE characters.author_id = p.id;

UPDATE public.contacts
SET profile_id = p.uid
FROM public.profiles p
WHERE contacts.profile_id = p.id;

UPDATE public.knowledge_bases
SET author_id = p.uid
FROM public.profiles p
WHERE knowledge_bases.author_id = p.id;

UPDATE public.chats
SET owner_id = p.uid
FROM public.profiles p
WHERE chats.owner_id = p.id;

UPDATE public.chat_members
SET profile_id = p.uid
FROM public.profiles p
WHERE chat_members.profile_id = p.id;

UPDATE public.messages
SET sender_profile_id = p.uid
FROM public.profiles p
WHERE messages.sender_profile_id = p.id;

-- Drop the old PK. CASCADE will automatically drop the FK constraints on dependent tables.
ALTER TABLE public.profiles
    DROP CONSTRAINT IF EXISTS profiles_pkey CASCADE;

-- Drop the unique constraint on uid (it will become PK, which implies unique)
ALTER TABLE public.profiles
    DROP CONSTRAINT IF EXISTS profiles_uid_key;

-- Drop the now unused surrogate 'id' column
ALTER TABLE public.profiles
    DROP COLUMN IF EXISTS id;

-- Promote 'uid' to Primary Key
ALTER TABLE public.profiles
    ADD PRIMARY KEY (uid);

ALTER TABLE public.settings
    ADD CONSTRAINT settings_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.plugins
    ADD CONSTRAINT plugins_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.plugin_subscriptions
    ADD CONSTRAINT plugin_subscriptions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.plugin_likes
    ADD CONSTRAINT plugin_likes_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.characters
    ADD CONSTRAINT characters_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.contacts
    ADD CONSTRAINT contacts_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.knowledge_bases
    ADD CONSTRAINT knowledge_bases_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.chats
    ADD CONSTRAINT chats_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.chat_members
    ADD CONSTRAINT chat_members_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;
ALTER TABLE public.messages
    ADD CONSTRAINT messages_sender_profile_id_fkey FOREIGN KEY (sender_profile_id) REFERENCES public.profiles (uid) ON DELETE CASCADE;

DROP TABLE IF EXISTS public.refresh_tokens;

CREATE TABLE public.refresh_tokens
(
    id         UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    user_id    UUID                     NOT NULL REFERENCES public.users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64)              NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    is_revoked BOOLEAN                  NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS refresh_tokens_token_hash_idx ON public.refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS refresh_tokens_user_id_idx ON public.refresh_tokens (user_id);