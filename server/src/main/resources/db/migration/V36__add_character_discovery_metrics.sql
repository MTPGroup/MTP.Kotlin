-- Character discovery metrics and favorites

ALTER TABLE public.characters
    ADD COLUMN IF NOT EXISTS tags text NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS favorite_count integer NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS chat_count integer NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS public.character_favorites (
    user_id uuid NOT NULL REFERENCES public.profiles(uid) ON DELETE CASCADE,
    character_id uuid NOT NULL REFERENCES public.characters(id) ON DELETE CASCADE,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, character_id)
);

CREATE INDEX IF NOT EXISTS idx_character_favorites_character_id
    ON public.character_favorites (character_id);

CREATE INDEX IF NOT EXISTS idx_characters_popular_sort
    ON public.characters (favorite_count DESC, chat_count DESC, updated_at DESC);

CREATE OR REPLACE FUNCTION public.update_character_favorite_count()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    IF (tg_op = 'INSERT') THEN
        UPDATE public.characters
        SET favorite_count = favorite_count + 1
        WHERE id = new.character_id;
    ELSIF (tg_op = 'DELETE') THEN
        UPDATE public.characters
        SET favorite_count = GREATEST(favorite_count - 1, 0)
        WHERE id = old.character_id;
    END IF;
    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS on_character_favorite_change ON public.character_favorites;
CREATE TRIGGER on_character_favorite_change
AFTER INSERT OR DELETE ON public.character_favorites
FOR EACH ROW EXECUTE FUNCTION public.update_character_favorite_count();

CREATE OR REPLACE FUNCTION public.sync_character_chat_count()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
BEGIN
    IF (tg_op = 'INSERT') THEN
        IF (new.character_id IS NOT NULL AND upper(new.member_type) = 'CHARACTER') THEN
            UPDATE public.characters
            SET chat_count = chat_count + 1
            WHERE id = new.character_id;
        END IF;
    ELSIF (tg_op = 'DELETE') THEN
        IF (old.character_id IS NOT NULL AND upper(old.member_type) = 'CHARACTER') THEN
            UPDATE public.characters
            SET chat_count = GREATEST(chat_count - 1, 0)
            WHERE id = old.character_id;
        END IF;
    ELSIF (tg_op = 'UPDATE') THEN
        IF (old.character_id IS NOT NULL AND upper(old.member_type) = 'CHARACTER') THEN
            UPDATE public.characters
            SET chat_count = GREATEST(chat_count - 1, 0)
            WHERE id = old.character_id;
        END IF;

        IF (new.character_id IS NOT NULL AND upper(new.member_type) = 'CHARACTER') THEN
            UPDATE public.characters
            SET chat_count = chat_count + 1
            WHERE id = new.character_id;
        END IF;
    END IF;

    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS on_chat_members_character_count_change ON public.chat_members;
CREATE TRIGGER on_chat_members_character_count_change
AFTER INSERT OR DELETE OR UPDATE ON public.chat_members
FOR EACH ROW EXECUTE FUNCTION public.sync_character_chat_count();

UPDATE public.characters c
SET chat_count = COALESCE(src.cnt, 0)
FROM (
         SELECT cm.character_id, COUNT(*)::integer AS cnt
         FROM public.chat_members cm
         WHERE cm.character_id IS NOT NULL
           AND upper(cm.member_type) = 'CHARACTER'
         GROUP BY cm.character_id
     ) src
WHERE c.id = src.character_id;

UPDATE public.characters c
SET chat_count = 0
WHERE NOT EXISTS (
    SELECT 1
    FROM public.chat_members cm
    WHERE cm.character_id = c.id
      AND upper(cm.member_type) = 'CHARACTER'
);
