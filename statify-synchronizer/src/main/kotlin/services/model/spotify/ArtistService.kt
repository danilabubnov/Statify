package org.danila.services.model.spotify

import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ARTISTS_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.artist.Artist
import org.danila.repository.ArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class ArtistService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val artistRepository: ArtistRepository
) {

    suspend fun findExistingArtists(ids: Set<String>): List<Artist> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            artistRepository.findArtistsBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleArtists(artists: Collection<Artist>): Collection<String> =
        artists
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_ARTISTS_CHUNK_SIZE)
            .flatMap { chunk ->
                DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                    artistRepository.upsertAndReturnSimpleArtists(chunk).awaitList()
                }
            }

}