package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.album.AlbumImage
import org.danila.repository.AlbumImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AlbumImageService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val albumImageRepository: AlbumImageRepository
) {

    suspend fun findExistingAlbumImages(albumIdImages: Set<Pair<String, List<ImageDTO>>>): List<AlbumImage> =
        withContext(Dispatchers.IO) {
            readSemaphore.withPermit { albumImageRepository.selectBatch(albumIdImages.map { it.first to it.second.map { it.url } }.toSet()).collectList().awaitSingle() }
        }

    suspend fun persistAlbumImages(albumImages: Collection<AlbumImage>): Unit =
        withContext(Dispatchers.IO) {
            writeSemaphore.withPermit {
                albumImages.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
                    transactionalOperator.executeAndAwait {
                        albumImageRepository.insertBatch(chunk)
                            .awaitSingleOrNull()
                    }
                }
            }
        }

}