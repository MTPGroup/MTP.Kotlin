ALTER TABLE public.characters
    ADD COLUMN IF NOT EXISTS example_messages text NOT NULL DEFAULT '[]';
