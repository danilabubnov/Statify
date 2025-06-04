package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.album.AlbumImage
import org.danila.repository.AlbumImageRepository
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AlbumImageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val albumImageRepository: AlbumImageRepository
) {

    suspend fun findExistingAlbumImages(albumIdImages: Set<Pair<String, List<ImageDTO>>>): List<AlbumImage> =
        databaseExecutionContext.withRead {
            albumImageRepository.selectBatch(
                albumIdImages.map { it.first to it.second.map { it.url } }.toSet()
            ).awaitList()
        }

    suspend fun persistAlbumImages(albumImages: Collection<AlbumImage>) {
        albumImages.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                albumImageRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}