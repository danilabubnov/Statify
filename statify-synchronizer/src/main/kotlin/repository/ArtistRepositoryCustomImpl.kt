package org.danila.repository

import org.danila.model.spotify.artist.Artist
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class ArtistRepositoryCustomImpl(
    val databaseClient: DatabaseClient,
) : ArtistRepositoryCustom {

    override fun upsertAndReturnSimpleArtists(artists: Collection<Artist>): Flux<String> {
        if (artists.isEmpty()) return Flux.empty()

        val placeholders = artists.indices.joinToString(", ") { index ->
            "(" + ((index * 4 + 1)..(index * 4 + 4)).joinToString(", ") { "$$it" } + ")"
        }

        val sql = """
            WITH inserted AS (
                INSERT INTO artists (
                    spotify_id,
                    followers_total,
                    name,
                    popularity
                )
                VALUES $placeholders
                ON CONFLICT (spotify_id) 
                DO UPDATE
                    SET
                        followers_total = EXCLUDED.followers_total,
                        popularity      = EXCLUDED.popularity
                    WHERE
                        artists.followers_total IS DISTINCT FROM EXCLUDED.followers_total OR
                        artists.popularity      IS DISTINCT FROM EXCLUDED.popularity
                    RETURNING
                        spotify_id,
                        (followers_total IS NULL OR popularity IS NULL) AS is_simple
            )
            SELECT spotify_id
                FROM inserted
            WHERE is_simple
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        artists.forEachIndexed { index, artist ->
            val followersTotal = artist.followersTotal
            val popularity = artist.popularity
            val base = index * 4

            spec = spec
                .bind(base + 0, artist.spotifyId)
                .let { s ->
                    if (followersTotal == null) s.bindNull(base + 1, Int::class.java)
                    else s.bind(base + 1, followersTotal)
                }
                .bind(base + 2, artist.name)
                .let { s ->
                    if (popularity == null) s.bindNull(base + 3, Int::class.java)
                    else s.bind(base + 3, popularity)
                }
        }

        return spec
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

}