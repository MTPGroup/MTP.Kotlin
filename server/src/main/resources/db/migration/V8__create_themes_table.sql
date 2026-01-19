CREATE TABLE IF NOT EXISTS themes
(
    id             UUID                     NOT NULL,
    author_id      UUID                     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS themes_author_id_idx ON themes (author_id);
CREATE INDEX IF NOT EXISTS themes_download_count_idx ON themes (download_count DESC);