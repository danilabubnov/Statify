package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.dto.spotify.common.ImageDTO
import org.danila.model.spotify.artist.ArtistImage
import org.danila.repository.ArtistImageRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ArtistImageStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val artistImageRepository: ArtistImageRepository
) {

    suspend fun findExistingArtistImages(artistIdImages: Set<Pair<String, List<ImageDTO>>>): List<ArtistImage> =
        databaseExecutionContext.withRead {
            artistImageRepository.selectBatch(
                artistIdImages.map { it.first to it.second.map { it.url } }.toSet()
            ).awaitList()
        }

    suspend fun persistArtistImage(artistImages: Collection<ArtistImage>): Unit =
        artistImages.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                artistImageRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }

}