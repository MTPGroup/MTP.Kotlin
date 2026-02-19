ALTER TABLE chats ADD COLUMN temporary boolean NOT NULL DEFAULT false;
ALTER TABLE chats ADD COLUMN expires_at timestamptz;
CREATE INDEX idx_chats_temporary_expires ON chats (temporary, expires_at) WHERE temporary = true;
