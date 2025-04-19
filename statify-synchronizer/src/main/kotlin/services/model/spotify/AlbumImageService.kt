package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingle
import org.danila.dto.common.ImageDTO
import org.danila.model.spotify.album.AlbumImage
import org.danila.repository.AlbumImageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Service
class AlbumImageService @Autowired constructor(
    private val transactionalOperator: TransactionalOperator,
    private val albumImageRepository: AlbumImageRepository
) {

    suspend fun findExistingAlbumImages(albumIdImages: Set<Pair<String, List<ImageDTO>>>): List<AlbumImage> =
        albumImageRepository.selectBatch(albumIdImages.map { it.first to it.second.map { it.url }}.toSet()).collectList().awaitSingle()

    suspend fun persistAlbumImages(albumImages: Collection<AlbumImage>): Collection<AlbumImage> =
        transactionalOperator.executeAndAwait {
            albumImages.chunked(300)
                .map { chunk ->
                    albumImageRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}