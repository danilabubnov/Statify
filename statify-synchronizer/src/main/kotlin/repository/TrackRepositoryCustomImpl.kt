package org.danila.repository

import org.danila.model.spotify.track.Track
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class TrackRepositoryCustomImpl(val databaseClient: DatabaseClient) : TrackRepositoryCustom {

    override fun upsertBatch(tracks: Collection<Track>): Flux<Track> {
        if (tracks.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO tracks (
                spotify_id,
                duration_ms,
                explicit,
                name,
                popularity,
                track_number,
                album_id
            )
            VALUES ${
                tracks.indices.joinToString(", ") { index ->
                    "($${index * 7 + 1}, $${index * 7 + 2}, $${index * 7 + 3}, $${index * 7 + 4}, $${index * 7 + 5}, $${index * 7 + 6}, $${index * 7 + 7})"
                }
            }
            ON CONFLICT (spotify_id)
            DO UPDATE
            SET
                popularity = EXCLUDED.popularity
        """.trimIndent()

        var statement = databaseClient.sql(sql)

        tracks.forEachIndexed { index, track ->
            val popularity = track.popularity

            statement = statement
                .bind(index * 7 + 0, track.spotifyId)
                .bind(index * 7 + 1, track.durationMs)
                .bind(index * 7 + 2, track.explicit)
                .bind(index * 7 + 3, track.name)
                .let {
                    if (popularity == null) it.bindNull(index * 7 + 4, Int::class.java)
                    else it.bind(index * 7 + 4, popularity)
                }
                .bind(index * 7 + 5, track.trackNumber)
                .bind(index * 7 + 6, track.albumId)
        }

        return statement.fetch()
            .rowsUpdated()
            .flatMapMany { Flux.fromIterable(tracks) }
    }

}