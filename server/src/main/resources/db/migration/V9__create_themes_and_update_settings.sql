-- Create themes table
CREATE TABLE IF NOT EXISTS public.themes
(
    id             UUID                     NOT NULL,
    author_id      UUID                     NOT NULL REFERENCES public.users (id) ON DELETE CASCADE,
    name           VARCHAR(64)              NOT NULL,
    description    VARCHAR(512),
    preview_url    VARCHAR(1024),
    data           JSONB                    NOT NULL,
    download_count INT                      NOT NULL DEFAULT 0,
    version        VARCHAR(32)              NOT NULL DEFAULT '1.0.0',
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS themes_author_id_idx ON public.themes (author_id);
CREATE INDEX IF NOT EXISTS themes_download_count_idx ON public.themes (download_count DESC);

-- Update settings table structure
ALTER TABLE public.settings
    ADD COLUMN IF NOT EXISTS active_theme_id      UUID,
    ADD COLUMN IF NOT EXISTS active_llm_config_id UUID;

-- Add foreign key constraints
ALTER TABLE public.settings
    ADD CONSTRAINT fk_settings_active_theme_id FOREIGN KEY (active_theme_id) REFERENCES public.themes (id) ON DELETE SET NULL;

ALTER TABLE public.settings
    ADD CONSTRAINT fk_settings_active_llm_config_id FOREIGN KEY (active_llm_config_id) REFERENCES public.llm_configs (id) ON DELETE SET NULL;