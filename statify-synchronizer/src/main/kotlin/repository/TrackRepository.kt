package org.danila.repository

import org.danila.model.spotify.track.Track
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TrackRepository : ReactiveCrudRepository<Track, String>, TrackRepositoryCustom {

    fun findTracksBySpotifyIdIn(trackIds: Set<String>): Flux<Track>

}

interface TrackRepositoryCustom {
    fun upsertBatch(tracks: Collection<Track>): Flux<Track>
}