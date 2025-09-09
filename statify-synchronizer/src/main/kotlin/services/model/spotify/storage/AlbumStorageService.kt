package org.danila.services.model.spotify.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.variables.SpotifyChunkSizes.MAX_PENDING_ALBUMS_FETCH_CHUNK_SIZE
import org.danila.configuration.variables.SpotifyChunkSizes.MAX_SAVED_ALBUMS_CHUNK_SIZE
import org.danila.dto.musicbrainz.release.AlbumReleaseGroupMapping
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumBarcodes
import org.danila.repository.AlbumRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AlbumStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val albumRepository: AlbumRepository,
) {

    suspend fun findExistingAlbums(ids: Set<String>): List<Album> =
        databaseExecutionContext.withRead {
            albumRepository.findAlbumsBySpotifyIdIn(ids).awaitList()
        }

    suspend fun findAlbumsWithBarcode(albumIds: Set<String>): List<AlbumBarcodes> =
        databaseExecutionContext.withRead {
            albumRepository.findAlbumsWithBarcode(albumIds).awaitList()
        }

    suspend fun claimPendingBatch(): Flow<List<String>> = flow {
        while (true) {
            val ids = databaseExecutionContext.withWriteTransactionRetry {
                albumRepository.claimPendingBatch(MAX_PENDING_ALBUMS_FETCH_CHUNK_SIZE)
                    .collectList()
                    .awaitSingle()
            }

            if (ids.isEmpty()) break
            else emit(ids)
        }
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

    suspend fun persistReleaseGroupForAlbums(albums: List<AlbumReleaseGroupMapping>) {
        databaseExecutionContext.withWriteTransactionRetry {
            albumRepository.persistReleaseGroupsForAlbums(albums).awaitSingleOrNull()
        }
    }

}