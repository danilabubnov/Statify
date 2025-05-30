package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.AlbumArtist
import org.danila.repository.AlbumArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class AlbumArtistService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val albumArtistsRepository: AlbumArtistRepository,
) {

    suspend fun findExistingAlbumArtists(ids: Set<Pair<String, String>>): List<AlbumArtist> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            albumArtistsRepository.findByAlbumArtistPairs(ids).awaitList()
        }

    suspend fun persistAlbumArtists(albumArtists: Collection<AlbumArtist>) {
        albumArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                albumArtistsRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}