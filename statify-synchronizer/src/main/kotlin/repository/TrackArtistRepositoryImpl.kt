package org.danila.repository

import org.danila.model.spotify.TrackArtist
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class TrackArtistRepositoryImpl(
    private val databaseClient: DatabaseClient
) : TrackArtistRepository {

    override fun insert(trackArtist: TrackArtist): Mono<TrackArtist> {
        return databaseClient
            .sql(
                """
                INSERT INTO track_artists (track_id, artist_id)
                VALUES (:trackId, :artistId)
                """.trimIndent()
            )
            .bind("trackId", trackArtist.trackId)
            .bind("artistId", trackArtist.artistId)
            .fetch()
            .rowsUpdated()
            .thenReturn(trackArtist)
    }

    override fun findByTrackArtistPairs(pairs: Set<Pair<String, String>>): Flux<TrackArtist> {
        if (pairs.isEmpty()) return Flux.empty()

        val whereClause = pairs.indices.joinToString(" OR ") {
            "(track_id = :trackId$it AND artist_id = :artistId$it)"
        }

        val sql = """
            SELECT track_id, artist_id 
            FROM track_artists 
            WHERE $whereClause
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        pairs.forEachIndexed { i, (albumId, artistId) ->
            spec = spec.bind("trackId$i", albumId)
                .bind("artistId$i", artistId)
        }

        return spec
            .map { row, _ ->
                TrackArtist(
                    trackId = row.get("track_id", String::class.java)!!,
                    artistId = row.get("artist_id", String::class.java)!!
                )
            }
            .all()
    }

    override fun insertBatch(trackArtists: Collection<TrackArtist>): Flux<TrackArtist> {
        if (trackArtists.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO track_artists (track_id, artist_id) 
            VALUES ${trackArtists.indices.joinToString(", ") { index -> "($${index * 2 + 1}, $${index * 2 + 2})" }}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        trackArtists.forEachIndexed { index, item ->
            boundStatement = boundStatement.bind(index * 2 + 0, item.trackId)
            boundStatement = boundStatement.bind(index * 2 + 1, item.artistId)
        }

        return boundStatement.fetch()
            .rowsUpdated()
            .flatMapMany { _ -> Flux.fromIterable(trackArtists) }
    }

}