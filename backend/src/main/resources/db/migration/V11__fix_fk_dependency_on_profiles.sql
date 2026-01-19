-- Fix: Repoint Foreign Keys from 'profiles' to 'users' to prevent cascade deletion during User updates.
-- Spring Data JDBC may delete/re-insert 'profiles' rows when updating a User, causing data loss for tables referencing 'profiles'.

-- Settings (Column was renamed to 'uid' in V9)
-- Drop the old constraint (name from V4 migration)
ALTER TABLE public.settings
    DROP CONSTRAINT IF EXISTS settings_owner_id_fkey;
ALTER TABLE public.settings
    ADD CONSTRAINT fk_settings_users
        FOREIGN KEY (uid)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Plugins
ALTER TABLE public.plugins
    DROP CONSTRAINT IF EXISTS plugins_author_id_fkey;
ALTER TABLE public.plugins
    ADD CONSTRAINT fk_plugins_users
        FOREIGN KEY (author_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Plugin Subscriptions
ALTER TABLE public.plugin_subscriptions
    DROP CONSTRAINT IF EXISTS plugin_subscriptions_user_id_fkey;
ALTER TABLE public.plugin_subscriptions
    ADD CONSTRAINT fk_plugin_subscriptions_users
        FOREIGN KEY (user_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Plugin Likes
ALTER TABLE public.plugin_likes
    DROP CONSTRAINT IF EXISTS plugin_likes_user_id_fkey;
ALTER TABLE public.plugin_likes
    ADD CONSTRAINT fk_plugin_likes_users
        FOREIGN KEY (user_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Characters
ALTER TABLE public.characters
    DROP CONSTRAINT IF EXISTS characters_author_id_fkey;
ALTER TABLE public.characters
    ADD CONSTRAINT fk_characters_users
        FOREIGN KEY (author_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Contacts
ALTER TABLE public.contacts
    DROP CONSTRAINT IF EXISTS contacts_profile_id_fkey;
ALTER TABLE public.contacts
    ADD CONSTRAINT fk_contacts_users
        FOREIGN KEY (profile_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Knowledge Bases
ALTER TABLE public.knowledge_bases
    DROP CONSTRAINT IF EXISTS knowledge_bases_author_id_fkey;
ALTER TABLE public.knowledge_bases
    ADD CONSTRAINT fk_knowledge_bases_users
        FOREIGN KEY (author_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Chats
ALTER TABLE public.chats
    DROP CONSTRAINT IF EXISTS chats_owner_id_fkey;
ALTER TABLE public.chats
    ADD CONSTRAINT fk_chats_users
        FOREIGN KEY (owner_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Chat Members
ALTER TABLE public.chat_members
    DROP CONSTRAINT IF EXISTS chat_members_profile_id_fkey;
ALTER TABLE public.chat_members
    ADD CONSTRAINT fk_chat_members_users
        FOREIGN KEY (profile_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

-- Messages
ALTER TABLE public.messages
    DROP CONSTRAINT IF EXISTS messages_sender_profile_id_fkey;
ALTER TABLE public.messages
    ADD CONSTRAINT fk_messages_users
        FOREIGN KEY (sender_profile_id)
            REFERENCES public.users (id)
            ON DELETE CASCADE;

DROP TABLE public.otp_codes;