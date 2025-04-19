package org.danila.repository

import org.danila.model.spotify.artist.ArtistGenre
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class ArtistGenreRepository(private val databaseClient: DatabaseClient) {

    fun insertBatch(artistGenres: Collection<ArtistGenre>): Flux<ArtistGenre> {
        if (artistGenres.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO artist_genres (artist_id, genre)   
            VALUES ${artistGenres.indices.joinToString(", ") { index -> "($${index * 2 + 1}, $${index * 2 + 2})" }}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        artistGenres.forEachIndexed { index, item ->
            boundStatement = boundStatement.bind(index * 2 + 0, item.artistId)
            boundStatement = boundStatement.bind(index * 2 + 1, item.genre)
        }

        return boundStatement.fetch()
            .rowsUpdated()
            .flatMapMany { _ -> Flux.fromIterable(artistGenres) }
    }

    fun selectBatch(artistIdGenres: Set<Pair<String, List<String>>>): Flux<ArtistGenre> {
        if (artistIdGenres.isEmpty()) return Flux.empty()

        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()

        artistIdGenres.forEach { (artistId, genres) ->
            if (genres.isNotEmpty()) {
                val artistParamIndex = params.size + 1
                params.add(artistId)

                val genreParamIndices = genres.indices.map { artistParamIndex + 1 + it }
                val genrePlaceholders = genreParamIndices.joinToString(", ") { "$$it" }
                conditions.add("(artist_id = $$artistParamIndex AND genre IN ($genrePlaceholders))")

                params.addAll(genres)
            }
        }

        if (conditions.isEmpty()) return Flux.empty()

        val sql = """
            SELECT artist_id, genre
            FROM artist_genres
            WHERE ${conditions.joinToString(" OR ")}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        params.forEachIndexed { index, param ->
            boundStatement = boundStatement.bind(index, param)
        }

        return boundStatement.map { row, _ ->
            ArtistGenre(
                artistId = row.get("artist_id", String::class.java)!!,
                genre = row.get("genre", String::class.java)!!
            )
        }.all()
    }

}