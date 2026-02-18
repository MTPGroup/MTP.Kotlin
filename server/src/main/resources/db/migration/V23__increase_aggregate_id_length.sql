alter table public.outbox_events
    alter column aggregate_id type varchar(50),
    alter column aggregate_type type varchar(50);
