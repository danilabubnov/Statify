package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.artist.ArtistImage
import org.danila.repository.ArtistImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistImageService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistImageRepository: ArtistImageRepository
) {

    suspend fun findExistingArtistImages(artistIdImages: Set<Pair<String, List<ImageDTO>>>): List<ArtistImage> =
        readSemaphore.withPermit { artistImageRepository.selectBatch(artistIdImages.map { it.first to it.second.map { it.url } }.toSet()).collectList().awaitSingle() }

    suspend fun persistArtistImage(artistImages: Collection<ArtistImage>): Unit =
        writeSemaphore.withPermit {
            transactionalOperator.executeAndAwait {
                artistImageRepository.insertBatch(artistImages)
                    .awaitSingleOrNull()
            }
        }

}