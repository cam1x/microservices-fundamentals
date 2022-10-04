CREATE TABLE IF NOT EXISTS songs (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    artist TEXT NOT NULL,
    album TEXT,
    length TEXT NOT NULL,
    resource_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    UNIQUE(name, artist, album, resource_id)
);