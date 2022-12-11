CREATE TYPE storage_type AS ENUM ('STAGING', 'PERMANENT');
CREATE CAST (character varying AS storage_type) WITH INOUT AS IMPLICIT;

CREATE TABLE IF NOT EXISTS storages (
    id BIGSERIAL PRIMARY KEY,
    type storage_type NOT NULL,
    bucket TEXT NOT NULL,
    path TEXT NOT NULL,
    UNIQUE(type, bucket, path)
);