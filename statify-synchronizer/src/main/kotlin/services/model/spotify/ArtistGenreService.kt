package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.repository.ArtistGenreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistGenreService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistGenreRepository: ArtistGenreRepository
) {

    suspend fun findExistingArtistGenres(artistIdGenres: Set<Pair<String, List<String>>>): List<ArtistGenre> =
        withContext(Dispatchers.IO) {
            readSemaphore.withPermit { artistGenreRepository.selectBatch(artistIdGenres).collectList().awaitSingle() }
        }

    suspend fun persistArtistGenres(artistGenres: Collection<ArtistGenre>): Unit =
        withContext(Dispatchers.IO) {
            writeSemaphore.withPermit {
                artistGenres.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
                    transactionalOperator.executeAndAwait {
                        artistGenreRepository.insertBatch(chunk)
                            .awaitSingleOrNull()
                    }
                }
            }
        }

}