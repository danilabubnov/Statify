package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.model.spotify.artist.ArtistGenre
import org.danila.repository.ArtistGenreRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class ArtistGenreService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val artistGenreRepository: ArtistGenreRepository
) {

    suspend fun findExistingArtistGenres(artistIdGenres: Set<Pair<String, List<String>>>): List<ArtistGenre> =
        artistGenreRepository.selectBatch(artistIdGenres).collectList().awaitSingle()

    suspend fun persistArtistGenres(artistGenres: Collection<ArtistGenre>): Collection<ArtistGenre> =
        transactionalOperator.executeAndAwait {
            artistGenres.chunked(1000)
                .map { chunk ->
                    artistGenreRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}