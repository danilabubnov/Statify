
DROP INDEX IF EXISTS idx_albums_mb_release_group_status_spotify_id;
DROP INDEX IF EXISTS idx_albums_mb_release_group_status_processing_started_at;

CREATE INDEX IF NOT EXISTS idx_arg_status ON album_release_groups (mb_release_group_status);