package org.danila.repository

import org.danila.model.spotify.album.Album
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AlbumRepository : ReactiveCrudRepository<Album, String>, AlbumRepositoryCustom {

    fun findAlbumsBySpotifyIdIn(ids: Set<String>): Flux<Album>

}

interface AlbumRepositoryCustom {
    fun upsertBatch(albums: Collection<Album>): Flux<Album>
}
