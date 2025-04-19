package org.danila.repository

import org.danila.model.spotify.album.UserFavoriteAlbum
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Repository
class UserFavoriteAlbumRepository(val databaseClient: DatabaseClient) {

    fun insertBatch(
        userFavoriteAlbums: Collection<UserFavoriteAlbum>,
    ): Flux<UserFavoriteAlbum> {
        if (userFavoriteAlbums.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO user_favorite_albums (user_id, album_id, added_at)
            VALUES ${userFavoriteAlbums.indices.joinToString(", ") { i -> "($${i * 3 + 1}, $${i * 3 + 2}, $${i * 3 + 3})" }}
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        userFavoriteAlbums.forEachIndexed { i, fav ->
            spec = spec
                .bind(i * 3 + 0, fav.userId)
                .bind(i * 3 + 1, fav.albumId)
                .bind(i * 3 + 2, fav.addedAt)
        }

        return spec
            .fetch()
            .rowsUpdated()
            .flatMapMany { Flux.fromIterable(userFavoriteAlbums) }
    }

    fun findUserFavoriteAlbumsByUserId(userId: UUID): Flux<UserFavoriteAlbum> {
        val sql = """
            SELECT user_id, album_id, added_at
            FROM user_favorite_albums
            WHERE user_id = $1
        """.trimIndent()

        return databaseClient
            .sql(sql)
            .bind(0, userId)
            .map { row, _ ->
                UserFavoriteAlbum(
                    userId  = row.get("user_id",  UUID::class.java)!!,
                    albumId = row.get("album_id", String::class.java)!!,
                    addedAt = row.get("added_at", Instant::class.java)!!
                )
            }
            .all()
    }

}