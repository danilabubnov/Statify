package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.repository.ArtistGenreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class ArtistGenreService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistGenreRepository: ArtistGenreRepository
) {

    suspend fun findExistingArtistGenres(artistIdGenres: Set<Pair<String, List<String>>>): List<ArtistGenre> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            artistGenreRepository.selectBatch(artistIdGenres).awaitList()
        }

    suspend fun persistArtistGenres(artistGenres: Collection<ArtistGenre>) {
        artistGenres.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                artistGenreRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}