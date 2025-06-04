package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.AlbumArtist
import org.danila.repository.AlbumArtistRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AlbumArtistStorageService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val albumArtistsRepository: AlbumArtistRepository,
) {

    suspend fun findExistingAlbumArtists(ids: Set<Pair<String, String>>): List<AlbumArtist> =
        databaseExecutionContext.withRead {
            albumArtistsRepository.findByAlbumArtistPairs(ids).awaitList()
        }

    suspend fun persistAlbumArtists(albumArtists: Collection<AlbumArtist>) {
        albumArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                albumArtistsRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}