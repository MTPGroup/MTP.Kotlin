-- Spring Modulith Event Publication Registry
CREATE TABLE IF NOT EXISTS event_publication
(
    id               UUID                     NOT NULL,
    completion_date  TIMESTAMP WITH TIME ZONE,
    event_type       VARCHAR(512)             NOT NULL,
    listener_id      VARCHAR(512)             NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    serialized_event TEXT                     NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);