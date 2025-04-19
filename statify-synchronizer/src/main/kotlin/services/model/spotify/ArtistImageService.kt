package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.artist.ArtistImage
import org.danila.repository.ArtistImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistImageService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val artistImageRepository: ArtistImageRepository
) {

    suspend fun findExistingArtistImages(artistIdImages: Set<Pair<String, List<ImageDTO>>>): List<ArtistImage> =
        artistImageRepository.selectBatch(artistIdImages.map { it.first to it.second.map { it.url }}.toSet()).collectList().awaitSingle()

    suspend fun persistArtistImage(artistImages: Collection<ArtistImage>): Collection<ArtistImage> =
        transactionalOperator.executeAndAwait {
            artistImages.chunked(300)
                .map { chunk ->
                    artistImageRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}