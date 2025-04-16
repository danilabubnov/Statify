package org.danila.repository

import org.danila.model.spotify.artist.Artist
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ArtistRepository : ReactiveCrudRepository<Artist, String>, ArtistRepositoryCustom {

    fun findArtistsBySpotifyIdIn(ids: Set<String>): Flux<Artist>

}

interface ArtistRepositoryCustom {
    fun upsertBatch(artists: Collection<Artist>): Flux<Artist>
}