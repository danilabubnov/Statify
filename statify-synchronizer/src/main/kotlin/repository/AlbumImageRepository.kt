package org.danila.repository

import org.danila.model.spotify.album.AlbumImage
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AlbumImageRepository : ReactiveCrudRepository<AlbumImage, String>, AlbumImageRepositoryCustom

interface AlbumImageRepositoryCustom {
    fun insertBatch(albumImages: Collection<AlbumImage>): Flux<AlbumImage>
    fun selectBatch(albumIdImages: Set<Pair<String, List<String>>>): Flux<AlbumImage>
}