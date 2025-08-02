package org.danila.repository

import org.danila.model.spotify.artist.UserFollowedArtist
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class UserFollowedArtistRepository(val databaseClient: DatabaseClient) {

    fun insertBatch(
        userFollowedArtists: Collection<UserFollowedArtist>
    ): Mono<Void> {
        if (userFollowedArtists.isEmpty()) return Mono.empty()

        val sql = """
            INSERT INTO user_followed_artists (user_id, artist_id)
            VALUES ${userFollowedArtists.indices.joinToString(", ") { i -> "($${i * 2 + 1}, $${i * 2 + 2})"}}
            ON CONFLICT (user_id, artist_id) DO NOTHING
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        userFollowedArtists.forEachIndexed { i, fav ->
            spec = spec
                .bind(i * 2 + 0, fav.userId)
                .bind(i * 2 + 1, fav.artistId)
        }

        return spec
            .fetch()
            .rowsUpdated()
            .then()
    }

    fun findUserFollowedArtistsByUserId(userId: UUID): Flux<UserFollowedArtist> {
        val sql = """
            SELECT user_id, artist_id
            FROM user_followed_artists
            WHERE user_id = $1
        """.trimIndent()

        return databaseClient
            .sql(sql)
            .bind(0, userId)
            .map { row, _ ->
                UserFollowedArtist(
                    userId = row.get("user_id", UUID::class.java)!!,
                    artistId = row.get("artist_id", String::class.java)!!
                )
            }
            .all()
    }

}