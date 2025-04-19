package org.danila.repository

import org.danila.model.spotify.track.UserFavoriteTrack
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Repository
class UserFavoriteTrackRepository(val databaseClient: DatabaseClient) {

    fun insertBatch(userFavoriteTracks: Collection<UserFavoriteTrack>): Flux<UserFavoriteTrack> {
        if (userFavoriteTracks.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO user_favorite_tracks (user_id, track_id, added_at)
            VALUES ${userFavoriteTracks.indices.joinToString(", ") { i -> "($${i * 3 + 1}, $${i * 3 + 2}, $${i * 3 + 3})" }}
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        userFavoriteTracks.forEachIndexed { i, fav ->
            spec = spec
                .bind(i * 3 + 0, fav.userId)
                .bind(i * 3 + 1, fav.trackId)
                .bind(i * 3 + 2, fav.addedAt)
        }

        return spec
            .fetch()
            .rowsUpdated()
            .flatMapMany { Flux.fromIterable(userFavoriteTracks) }
    }

    fun findUserFavoriteTracksByUserId(userId: UUID): Flux<UserFavoriteTrack> {
        val sql = """
            SELECT user_id, track_id, added_at
            FROM user_favorite_tracks
            WHERE user_id = $1
        """.trimIndent()

        return databaseClient
            .sql(sql)
            .bind(0, userId)
            .map { row, _ ->
                UserFavoriteTrack(
                    userId  = row.get("user_id",  UUID::class.java)!!,
                    trackId = row.get("track_id", String::class.java)!!,
                    addedAt = row.get("added_at", Instant::class.java)!!
                )
            }
            .all()
    }

}