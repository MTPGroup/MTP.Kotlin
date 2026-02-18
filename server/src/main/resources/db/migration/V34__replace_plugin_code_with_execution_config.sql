ALTER TABLE plugins ADD COLUMN execution_config jsonb;

UPDATE plugins SET execution_config = jsonb_build_object(
    'type', 'HTTP',
    'method', 'POST',
    'urlTemplate', code,
    'headers', '{}'::jsonb,
    'maxResponseLength', 8000
);

ALTER TABLE plugins ALTER COLUMN execution_config SET NOT NULL;
ALTER TABLE plugins DROP COLUMN code;
