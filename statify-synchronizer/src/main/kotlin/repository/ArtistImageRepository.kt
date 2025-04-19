package org.danila.repository

import org.danila.model.spotify.artist.ArtistImage
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class ArtistImageRepository(private val databaseClient: DatabaseClient) {

    fun insertBatch(artistImages: Collection<ArtistImage>): Flux<ArtistImage> {
        if (artistImages.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO artist_images (artist_id, image_height, image_url, image_width, image_order)  
            VALUES ${artistImages.indices.joinToString(", ") { index -> "($${index * 5 + 1}, $${index * 5 + 2}, $${index * 5 + 3}, $${index * 5 + 4}, $${index * 5 + 5})" }}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        artistImages.forEachIndexed { index, item ->
            boundStatement = boundStatement.bind(index * 5 + 0, item.artistId)
            boundStatement = boundStatement.bind(index * 5 + 1, item.imageHeight)
            boundStatement = boundStatement.bind(index * 5 + 2, item.imageUrl)
            boundStatement = boundStatement.bind(index * 5 + 3, item.imageWidth)
            boundStatement = boundStatement.bind(index * 5 + 4, item.imageOrder)
        }

        return boundStatement.fetch()
            .rowsUpdated()
            .flatMapMany { _ -> Flux.fromIterable(artistImages) }
    }

    fun selectBatch(artistIdImages: Set<Pair<String, List<String>>>): Flux<ArtistImage> {
        if (artistIdImages.isEmpty()) return Flux.empty()

        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()

        artistIdImages.forEach { (artistId, imageUrls) ->
            if (imageUrls.isNotEmpty()) {
                val artistParamIndex = params.size + 1
                params.add(artistId)

                val urlParamIndices = imageUrls.indices.map { artistParamIndex + 1 + it }
                val urlPlaceholders = urlParamIndices.joinToString(", ") { "$$it" }
                conditions.add("(artist_id = $$artistParamIndex AND image_url IN ($urlPlaceholders))")

                params.addAll(imageUrls)
            }
        }

        if (conditions.isEmpty()) return Flux.empty()

        val sql = """
            SELECT artist_id, image_order, image_url, image_height, image_width
            FROM artist_images
            WHERE ${conditions.joinToString(" OR ")}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        params.forEachIndexed { index, param ->
            boundStatement = boundStatement.bind(index, param)
        }

        return boundStatement.map { row, _ ->
            ArtistImage(
                artistId = row.get("artist_id", String::class.java)!!,
                imageOrder = row.get("image_order", Int::class.java)!!,
                imageUrl = row.get("image_url", String::class.java)!!,
                imageHeight = row.get("image_height", Int::class.java)!!,
                imageWidth = row.get("image_width", Int::class.java)!!
            )
        }.all()
    }

}