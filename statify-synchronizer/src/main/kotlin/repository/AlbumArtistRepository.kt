package org.danila.repository

import org.danila.model.spotify.AlbumArtist
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class AlbumArtistRepository(private val databaseClient: DatabaseClient) {

    fun findByAlbumArtistPairs(pairs: Set<Pair<String, String>>): Flux<AlbumArtist> {
        if (pairs.isEmpty()) return Flux.empty()

        val whereClause = pairs.indices.joinToString(" OR ") {
            "(album_id = :albumId$it AND artist_id = :artistId$it)"
        }

        val sql = """
            SELECT album_id, artist_id 
            FROM album_artists 
            WHERE $whereClause
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        pairs.forEachIndexed { i, (albumId, artistId) ->
            spec = spec.bind("albumId$i", albumId)
                .bind("artistId$i", artistId)
        }

        return spec
            .map { row, _ ->
                AlbumArtist(
                    albumId = row.get("album_id", String::class.java)!!,
                    artistId = row.get("artist_id", String::class.java)!!
                )
            }
            .all()
    }

    fun insertBatch(albumArtists: Collection<AlbumArtist>): Flux<AlbumArtist> {
        if (albumArtists.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO album_artists (album_id, artist_id) 
            VALUES ${albumArtists.indices.joinToString(", ") { index -> "($${index * 2 + 1}, $${index * 2 + 2})" }}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        albumArtists.forEachIndexed { index, item ->
            boundStatement = boundStatement.bind(index * 2 + 0, item.albumId)
            boundStatement = boundStatement.bind(index * 2 + 1, item.artistId)
        }

        return boundStatement.fetch()
            .rowsUpdated()
            .flatMapMany { _ -> Flux.fromIterable(albumArtists) }
    }

}