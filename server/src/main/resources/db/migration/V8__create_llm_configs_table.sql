ALTER TABLE public.settings
    RENAME COLUMN owner_id TO uid;

CREATE TABLE IF NOT EXISTS public.llm_configs
(
    id            UUID         NOT NULL,
    setting_id    UUID         NOT NULL REFERENCES settings (uid) ON DELETE CASCADE,
    provider      VARCHAR(32)  NOT NULL,
    base_url      VARCHAR(255) NOT NULL,
    api_key       VARCHAR(255) NOT NULL,
    model         VARCHAR(64)  NOT NULL,
    temperature   REAL         NOT NULL,
    max_tokens    INT,
    run_on_client BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS llm_configs_setting_id_idx ON llm_configs (setting_id);