package org.danila.repository

import org.danila.dto.musicbrainz.release.AlbumReleaseGroupMapping
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumBarcodes
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AlbumRepository : ReactiveCrudRepository<Album, String>, AlbumRepositoryCustom {

    fun findAlbumsBySpotifyIdIn(ids: Set<String>): Flux<Album>

}

interface AlbumRepositoryCustom {
    fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Flux<String>
    fun claimPendingBatch(limit: Int): Flux<String>
    fun findAlbumsWithBarcode(albumIds: Set<String>): Flux<AlbumBarcodes>
    fun persistReleaseGroupsForAlbums(albums: List<AlbumReleaseGroupMapping>): Mono<Void>
}
