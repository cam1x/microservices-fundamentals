CREATE TYPE storage_type AS ENUM ('STAGING', 'PERMANENT');

CREATE TABLE IF NOT EXISTS storages (
    id BIGSERIAL PRIMARY KEY,
    type storage_type NOT NULL,
    bucket TEXT NOT NULL,
    path TEXT NOT NULL,
    UNIQUE(type, bucket, path)
);