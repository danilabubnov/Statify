package org.danila.repository

import org.danila.model.spotify.artist.Artist
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class ArtistRepositoryCustomImpl(
    val databaseClient: DatabaseClient
) : ArtistRepositoryCustom {

    override fun upsertBatch(artists: Collection<Artist>): Flux<Artist> {
        if (artists.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO artists (
                spotify_id,
                followers_total,
                name,
                popularity
            )
            VALUES ${
                artists.indices.joinToString(", ") { index ->
                    "($${index * 4 + 1}, $${index * 4 + 2}, $${index * 4 + 3}, $${index * 4 + 4})"
                }
            }
            ON CONFLICT (spotify_id)
            DO UPDATE
            SET
                followers_total = EXCLUDED.followers_total,
                popularity = EXCLUDED.popularity
        """.trimIndent()

        var statement = databaseClient.sql(sql)

        artists.forEachIndexed { index, artist ->
            val followersTotal = artist.followersTotal
            val popularity = artist.popularity

            statement = statement
                .bind(index * 4 + 0, artist.spotifyId)
                .let {
                    if (followersTotal == null) it.bindNull(index * 4 + 1, Int::class.java)
                    else it.bind(index * 4 + 1, followersTotal)
                }
                .bind(index * 4 + 2, artist.name)
                .let {
                    if (popularity == null) it.bindNull(index * 4 + 3, Int::class.java)
                    else it.bind(index * 4 + 3, popularity)
                }
        }

        return statement.fetch()
            .rowsUpdated()
            .flatMapMany { Flux.fromIterable(artists) }
    }

}