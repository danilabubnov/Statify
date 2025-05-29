package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.danila.MAX_SAVED_ALBUMS_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.album.Album
import org.danila.repository.AlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AlbumService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val transactionalOperator: TransactionalOperator,
    private val albumRepository: AlbumRepository
) {

    suspend fun findExistingAlbum(ids: Set<String>): List<Album> =
        readSemaphore.withPermit { albumRepository.findAlbumsBySpotifyIdIn(ids).awaitList() }

    suspend fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Collection<String> =
        writeSemaphore.withPermit {
            albums.chunked(MAX_SAVED_ALBUMS_CHUNK_SIZE).flatMap { chunk ->
                transactionalOperator.executeAndAwait {
                    albumRepository.upsertAndReturnSimpleAlbums(chunk)
                        .collectList()
                        .awaitSingle()
                }
            }
        }

}