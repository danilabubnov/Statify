package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.artist.ArtistImage
import org.danila.repository.ArtistImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class ArtistImageService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistImageRepository: ArtistImageRepository
) {

    suspend fun findExistingArtistImages(artistIdImages: Set<Pair<String, List<ImageDTO>>>): List<ArtistImage> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            artistImageRepository.selectBatch(
                artistIdImages.map { it.first to it.second.map { it.url } }.toSet()
            ).awaitList()
        }

    suspend fun persistArtistImage(artistImages: Collection<ArtistImage>): Unit =
        artistImages.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                artistImageRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }

}