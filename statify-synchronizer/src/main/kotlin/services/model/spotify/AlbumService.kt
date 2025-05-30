package org.danila.services.model.spotify

import kotlinx.coroutines.sync.Semaphore
import org.danila.MAX_SAVED_ALBUMS_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.album.Album
import org.danila.repository.AlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator

@Service
class AlbumService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val albumRepository: AlbumRepository,
) {

    suspend fun findExistingAlbum(ids: Set<String>): List<Album> =
        DatabaseExecutionContext.withRead(readSemaphore = readSemaphore) {
            albumRepository.findAlbumsBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Collection<String> =
        albums
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_ALBUMS_CHUNK_SIZE)
            .flatMap { chunk ->
                DatabaseExecutionContext.withWriteTransactionRetry(writeSemaphore, transactionalOperator) {
                    albumRepository.upsertAndReturnSimpleAlbums(chunk).awaitList()
                }
            }


}