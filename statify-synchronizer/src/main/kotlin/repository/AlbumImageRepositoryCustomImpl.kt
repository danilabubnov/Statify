package org.danila.repository

import org.danila.model.spotify.album.AlbumImage
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class AlbumImageRepositoryCustomImpl(
    private val databaseClient: DatabaseClient
) : AlbumImageRepositoryCustom {

    override fun insertBatch(albumImages: Collection<AlbumImage>): Flux<AlbumImage> {
        if (albumImages.isEmpty()) return Flux.empty()

        val sql = """
            INSERT INTO album_images (album_id, image_height, image_url, image_width, image_order)  
            VALUES ${albumImages.indices.joinToString(", ") { index -> "($${index * 5 + 1}, $${index * 5 + 2}, $${index * 5 + 3}, $${index * 5 + 4}, $${index * 5 + 5})" }}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        albumImages.forEachIndexed { index, item ->
            boundStatement = boundStatement.bind(index * 5 + 0, item.albumId)
            boundStatement = boundStatement.bind(index * 5 + 1, item.imageHeight)
            boundStatement = boundStatement.bind(index * 5 + 2, item.imageUrl)
            boundStatement = boundStatement.bind(index * 5 + 3, item.imageWidth)
            boundStatement = boundStatement.bind(index * 5 + 4, item.imageOrder)
        }

        return boundStatement.fetch()
            .rowsUpdated()
            .flatMapMany { _ -> Flux.fromIterable(albumImages) }
    }

    override fun selectBatch(albumIdImages: Set<Pair<String, List<String>>>): Flux<AlbumImage> {
        if (albumIdImages.isEmpty()) return Flux.empty()

        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()

        albumIdImages.forEach { (albumId, imageUrls) ->
            if (imageUrls.isNotEmpty()) {
                val albumParamIndex = params.size + 1
                params.add(albumId)

                val urlParamIndices = imageUrls.indices.map { albumParamIndex + 1 + it }
                val urlPlaceholders = urlParamIndices.joinToString(", ") { "$$it" }
                conditions.add("(album_id = $$albumParamIndex AND image_url IN ($urlPlaceholders))")

                params.addAll(imageUrls)
            }
        }

        if (conditions.isEmpty()) return Flux.empty()

        val sql = """
            SELECT album_id, image_order, image_url, image_height, image_width
            FROM album_images
            WHERE ${conditions.joinToString(" OR ")}
        """.trimIndent()

        var boundStatement = databaseClient.sql(sql)

        params.forEachIndexed { index, param ->
            boundStatement = boundStatement.bind(index, param)
        }

        return boundStatement.map { row, _ ->
            AlbumImage(
                albumId = row.get("album_id", String::class.java)!!,
                imageOrder = row.get("image_order", Int::class.java)!!,
                imageUrl = row.get("image_url", String::class.java)!!,
                imageHeight = row.get("image_height", Int::class.java)!!,
                imageWidth = row.get("image_width", Int::class.java)!!
            )
        }.all()
    }

}