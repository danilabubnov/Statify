package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.TrackArtist
import org.danila.repository.TrackArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class TrackArtistService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val trackArtistsRepository: TrackArtistRepository
) {

    suspend fun findExistingTrackArtists(ids: Set<Pair<String, String>>): List<TrackArtist> =
        trackArtistsRepository.findByTrackArtistPairs(ids).awaitList()

    suspend fun persistTrackArtists(trackArtists: Collection<TrackArtist>): Collection<TrackArtist> =
        transactionalOperator.executeAndAwait {
            trackArtists.chunked(1000)
                .map { chunk ->
                    trackArtistsRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}