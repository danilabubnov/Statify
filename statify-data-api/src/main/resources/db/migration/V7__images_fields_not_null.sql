BEGIN;

UPDATE album_images
SET image_height = 0
WHERE image_height IS NULL;

UPDATE album_images
SET image_width = 0
WHERE image_width IS NULL;

DELETE FROM album_images
WHERE image_url IS NULL;

UPDATE artist_images
SET image_height = 0
WHERE image_height IS NULL;

UPDATE artist_images
SET image_width = 0
WHERE image_width IS NULL;

DELETE FROM artist_images
WHERE image_url IS NULL;

ALTER TABLE album_images
    ALTER COLUMN image_height SET NOT NULL,
    ALTER COLUMN image_width  SET NOT NULL,
    ALTER COLUMN image_url    SET NOT NULL;

ALTER TABLE artist_images
    ALTER COLUMN image_height SET NOT NULL,
    ALTER COLUMN image_width  SET NOT NULL,
    ALTER COLUMN image_url    SET NOT NULL;

COMMIT;
