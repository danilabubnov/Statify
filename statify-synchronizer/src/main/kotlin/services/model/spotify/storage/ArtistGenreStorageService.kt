package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.repository.ArtistGenreRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ArtistGenreStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val artistGenreRepository: ArtistGenreRepository
) {

    suspend fun findExistingArtistGenres(artistIdGenres: Set<Pair<String, List<String>>>): List<ArtistGenre> =
        databaseExecutionContext.withRead {
            artistGenreRepository.selectBatch(artistIdGenres).awaitList()
        }

    suspend fun persistArtistGenres(artistGenres: Collection<ArtistGenre>) {
        artistGenres.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                artistGenreRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}