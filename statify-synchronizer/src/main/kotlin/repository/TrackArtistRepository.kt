package org.danila.repository

import org.danila.model.spotify.TrackArtist
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TrackArtistRepository {

    fun insert(trackArtist: TrackArtist): Mono<TrackArtist>

    fun findByTrackArtistPairs(pairs: Set<Pair<String, String>>): Flux<TrackArtist>

    fun insertBatch(trackArtists: Collection<TrackArtist>): Flux<TrackArtist>

}