package org.danila.repository

import org.danila.model.spotify.album.Album
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class AlbumRepositoryCustomImpl(
    private val databaseClient: DatabaseClient
) : AlbumRepositoryCustom {

    override fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Flux<String> {
        if (albums.isEmpty()) return Flux.empty()

        val placeholders = albums.indices.joinToString(", ") { index ->
            "(" + ((index * 11 + 1)..(index * 11 + 11)).joinToString(", ") { "$$it" } + ")"
        }

        val sql = """
            WITH inserted AS (
                INSERT INTO albums (
                    spotify_id,
                    album_type,
                    label,
                    name,
                    popularity,
                    release_date_precision,
                    release_date_raw,
                    release_day,
                    release_month,
                    release_year,
                    total_tracks
                )
            VALUES $placeholders
            ON CONFLICT (spotify_id) 
            DO UPDATE
                SET
                    label         = EXCLUDED.label,
                    popularity    = EXCLUDED.popularity,
                    release_day   = EXCLUDED.release_day,
                    release_month = EXCLUDED.release_month
                WHERE 
                    albums.label         IS DISTINCT FROM EXCLUDED.label OR
                    albums.popularity    IS DISTINCT FROM EXCLUDED.popularity OR 
                    albums.release_day   IS DISTINCT FROM EXCLUDED.release_day OR
                    albums.release_month IS DISTINCT FROM EXCLUDED.release_month
                RETURNING
                    spotify_id,
                    (label IS NULL OR popularity IS NULL) AS is_simple
            )
            SELECT spotify_id
                FROM inserted
            WHERE is_simple
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albums.forEachIndexed { index, album ->
            val label = album.label
            val popularity = album.popularity
            val releaseDay = album.releaseDay
            val releaseMonth = album.releaseMonth
            val base = index * 11

            spec = spec
                .bind(base + 0, album.spotifyId)
                .bind(base + 1, album.albumType)
                .let { s ->
                    if (label == null) s.bindNull(base + 2, String::class.java)
                    else s.bind(base + 2, label)
                }
                .bind(base + 3, album.name)
                .let { s ->
                    if (popularity == null) s.bindNull(base + 4, Int::class.java)
                    else s.bind(base + 4, popularity)
                }
                .bind(base + 5, album.releaseDatePrecision)
                .bind(base + 6, album.releaseDateRaw)
                .let { s ->
                    if (releaseDay == null) s.bindNull(base + 7, Int::class.java)
                    else s.bind(base + 7, releaseDay)
                }
                .let { s ->
                    if (releaseMonth == null) s.bindNull(base + 8, Int::class.java)
                    else s.bind(base + 8, releaseMonth)
                }
                .bind(base + 9, album.releaseYear)
                .bind(base + 10, album.totalTracks)
        }

        return spec
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

}