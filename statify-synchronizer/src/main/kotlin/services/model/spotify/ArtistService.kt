package org.danila.services.model.spotify

import org.danila.MAX_SAVED_ARTISTS_CHUNK_SIZE
import org.danila.model.spotify.artist.Artist
import org.danila.repository.ArtistRepository
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ArtistService @Autowired constructor(
    private val databaseExecutionContext: DatabaseExecutionContext,
    private val artistRepository: ArtistRepository
) {

    suspend fun findExistingArtists(ids: Set<String>): List<Artist> =
        databaseExecutionContext.withRead {
            artistRepository.findArtistsBySpotifyIdIn(ids).awaitList()
        }

    suspend fun upsertAndReturnSimpleArtists(artists: Collection<Artist>): Collection<String> =
        artists
            .sortedBy { it.spotifyId }
            .chunked(MAX_SAVED_ARTISTS_CHUNK_SIZE)
            .flatMap { chunk ->
                databaseExecutionContext.withWriteTransactionRetry {
                    artistRepository.upsertAndReturnSimpleArtists(chunk).awaitList()
                }
            }

}