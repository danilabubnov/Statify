package org.danila.repository

import org.danila.model.spotify.track.Track
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class TrackRepositoryCustomImpl(val databaseClient: DatabaseClient) : TrackRepositoryCustom {

    override fun upsertAndReturnSimpleTracks(tracks: Collection<Track>): Flux<String> {
        if (tracks.isEmpty()) return Flux.empty()

        val placeholders = tracks.indices.joinToString(", ") { index ->
            "(" + ((index * 8 + 1)..(index * 8 + 8)).joinToString(", ") { "$$it" } + ")"
        }

        val sql = """
            WITH inserted AS (
                INSERT INTO tracks (
                spotify_id,
                duration_ms,
                explicit,
                name,
                popularity,
                track_number,
                album_id,
                isrc
            )
            VALUES $placeholders
            ON CONFLICT (spotify_id) 
            DO UPDATE
                SET 
                    popularity = EXCLUDED.popularity,
                    isrc       = EXCLUDED.isrc
                WHERE 
                    tracks.popularity IS DISTINCT FROM EXCLUDED.popularity OR
                    tracks.isrc       IS DISTINCT FROM EXCLUDED.isrc
                RETURNING
                    spotify_id,
                    (popularity IS NULL) AS is_simple
            )
        SELECT spotify_id
        FROM inserted
        WHERE is_simple
    """.trimIndent()

        var spec = databaseClient.sql(sql)
        tracks.forEachIndexed { index, track ->
            val popularity = track.popularity
            val isrc = track.isrc
            val base = index * 8

            spec = spec
                .bind(base + 0, track.spotifyId)
                .bind(base + 1, track.durationMs)
                .bind(base + 2, track.explicit)
                .bind(base + 3, track.name)
                .let { s ->
                    if (popularity == null) s.bindNull(base + 4, Int::class.java)
                    else s.bind(base + 4, popularity)
                }
                .bind(base + 5, track.trackNumber)
                .bind(base + 6, track.albumId)
                .let { s ->
                    if (isrc == null) s.bindNull(base + 7, String::class.java)
                    else s.bind(base + 7, isrc)
                }
        }

        return spec
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

}