package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.AlbumArtist
import org.danila.repository.AlbumArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AlbumArtistService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val albumArtistsRepository: AlbumArtistRepository
) {

    suspend fun findExistingAlbumArtists(ids: Set<Pair<String, String>>): List<AlbumArtist> =
        albumArtistsRepository.findByAlbumArtistPairs(ids).awaitList()

    suspend fun persistAlbumArtists(albumArtists: Collection<AlbumArtist>): Collection<AlbumArtist> =
        transactionalOperator.executeAndAwait {
            albumArtists.chunked(300)
                .map { chunk ->
                    albumArtistsRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}