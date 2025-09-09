CREATE INDEX IF NOT EXISTS idx_albums_mb_release_group_status_spotify_id
    ON albums (mb_release_group_status, spotify_id);

CREATE INDEX IF NOT EXISTS idx_albums_mb_release_group_status_processing_started_at
    ON albums (mb_release_group_status, processing_started_at);

CREATE INDEX IF NOT EXISTS idx_album_images_album_id_image_url
    ON album_images (album_id, image_url);

CREATE INDEX IF NOT EXISTS idx_artist_images_artist_id_image_url
    ON artist_images (artist_id, image_url);
