package org.danila.services.model.spotify

import org.danila.MAX_SAVED_ALBUMS_CHUNK_SIZE
import org.danila.model.spotify.album.Album
import org.danila.repository.AlbumRepository
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AlbumService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val albumRepository: AlbumRepository,
) {

    suspend fun findExistingAlbum(ids: Set<String>): List<Album> =
        databaseExecutionContext.withRead {
            albumRepository.findAlbumsBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Collection<String> =
        albums
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_ALBUMS_CHUNK_SIZE)
            .flatMap { chunk ->
                databaseExecutionContext.withWriteTransactionRetry {
                    albumRepository.upsertAndReturnSimpleAlbums(chunk).awaitList()
                }
            }


}