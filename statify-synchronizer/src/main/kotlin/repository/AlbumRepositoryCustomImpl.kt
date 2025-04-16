package org.danila.repository

import org.danila.model.spotify.album.Album
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class AlbumRepositoryCustomImpl(
    private val databaseClient: DatabaseClient
) : AlbumRepositoryCustom {

    override fun upsertBatch(albums: Collection<Album>): Flux<Album> {
        if (albums.isEmpty()) return Flux.empty()

        val sql = """
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
            VALUES ${
                albums.indices.joinToString(", ") { index ->
                    "($${index * 11 + 1}, $${index * 11 + 2}, $${index * 11 + 3}, $${index * 11 + 4}, $${index * 11 + 5}, $${index * 11 + 6}, $${index * 11 + 7}, $${index * 11 + 8}, $${index * 11 + 9}, $${index * 11 + 10}, $${index * 11 + 11})"
                }
            }
            ON CONFLICT (spotify_id)
            DO UPDATE
            SET
                label = EXCLUDED.label,
                popularity = EXCLUDED.popularity,
                release_day = EXCLUDED.release_day,
                release_month = EXCLUDED.release_month
        """.trimIndent()

        var statement = databaseClient.sql(sql)

        albums.forEachIndexed { index, album ->
            val label = album.label
            val popularity = album.popularity
            val releaseDay = album.releaseDay
            val releaseMonth = album.releaseMonth

            statement = statement
                .bind(index * 11 + 0, album.spotifyId)
                .bind(index * 11 + 1, album.albumType)
                .let {
                    if (label == null) it.bindNull(index * 11 + 2, String::class.java)
                    else it.bind(index * 11 + 2, label)
                }
                .bind(index * 11 + 3, album.name)
                .let {
                    if (popularity == null) it.bindNull(index * 11 + 4, Int::class.java)
                    else it.bind(index * 11 + 4, popularity)
                }
                .bind(index * 11 + 5, album.releaseDatePrecision)
                .bind(index * 11 + 6, album.releaseDateRaw)
                .let {
                    if (releaseDay == null) it.bindNull(index * 11 + 7, Int::class.java)
                    else it.bind(index * 11 + 7, releaseDay)
                }
                .let {
                    if (releaseMonth == null) it.bindNull(index * 11 + 8, Int::class.java)
                    else it.bind(index * 11 + 8, releaseMonth)
                }
                .bind(index * 11 + 9, album.releaseYear)
                .bind(index * 11 + 10, album.totalTracks)
        }

        return statement.fetch()
            .rowsUpdated()
            .flatMapMany { Flux.fromIterable(albums) }
    }

}