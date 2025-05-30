package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.album.AlbumImage
import org.danila.repository.AlbumImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class AlbumImageService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val albumImageRepository: AlbumImageRepository
) {

    suspend fun findExistingAlbumImages(albumIdImages: Set<Pair<String, List<ImageDTO>>>): List<AlbumImage> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            albumImageRepository.selectBatch(
                albumIdImages.map { it.first to it.second.map { it.url } }.toSet()
            ).awaitList()
        }

    suspend fun persistAlbumImages(albumImages: Collection<AlbumImage>) {
        albumImages.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                albumImageRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}