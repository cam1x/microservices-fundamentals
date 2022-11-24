ALTER TABLE resources
DROP COLUMN source_path;

ALTER TABLE resources
ADD COLUMN storage_id BIGINT NOT NULL;

ALTER TABLE resources
ADD COLUMN file_name TEXT NOT NULL;