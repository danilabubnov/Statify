package org.danila.repository

import org.danila.model.spotify.artist.ArtistGenre
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ArtistGenreRepository : ReactiveCrudRepository<ArtistGenre, String>, ArtistGenreRepositoryCustom

interface ArtistGenreRepositoryCustom {
    fun insertBatch(artistGenres: Collection<ArtistGenre>): Flux<ArtistGenre>
    fun selectBatch(artistIdGenres: Set<Pair<String, List<String>>>): Flux<ArtistGenre>
}