-- Drop the immediate constraint
ALTER TABLE public.settings DROP CONSTRAINT IF EXISTS fk_settings_active_llm_config_id;

-- Re-create as DEFERRABLE INITIALLY DEFERRED
ALTER TABLE public.settings
    ADD CONSTRAINT fk_settings_active_llm_config_id
    FOREIGN KEY (active_llm_config_id)
    REFERENCES public.llm_configs (id)
    ON DELETE SET NULL
    DEFERRABLE INITIALLY DEFERRED;