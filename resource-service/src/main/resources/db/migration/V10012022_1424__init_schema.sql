CREATE TABLE IF NOT EXISTS resources (
    id BIGSERIAL PRIMARY KEY,
    source_path TEXT NOT NULL,
    checksum TEXT NOT NULL,
    size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(source_path, checksum)
);