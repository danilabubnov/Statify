package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.album.Album
import org.danila.repository.AlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AlbumService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val albumRepository: AlbumRepository
) {

    suspend fun findExistingAlbum(ids: Set<String>): List<Album> = albumRepository.findAlbumsBySpotifyIdIn(ids).awaitList()

    suspend fun upsertAlbums(albums: Collection<Album>): Collection<Album> =
        transactionalOperator.executeAndAwait {
            albums.chunked(1000)
                .map { chunk ->
                    albumRepository.upsertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}