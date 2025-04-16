package org.danila.repository

import org.danila.model.spotify.artist.ArtistImage
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ArtistImageRepository : ReactiveCrudRepository<ArtistImage, String>, ArtistImageRepositoryCustom

interface ArtistImageRepositoryCustom {
    fun insertBatch(artistImages: Collection<ArtistImage>): Flux<ArtistImage>
    fun selectBatch(artistIdImages: Set<Pair<String, List<String>>>): Flux<ArtistImage>
}