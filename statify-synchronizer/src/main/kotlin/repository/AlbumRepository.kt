package org.danila.repository

import org.danila.dto.musicbrainz.releasegroup.AlbumReleaseGroupLookupResult
import org.danila.model.spotify.album.Album
import org.danila.repository.projection.album.AlbumBarcodes
import org.danila.repository.projection.album.AlbumNameLookup
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
    fun claimPendingAlbums(limit: Int): Flux<String>
    fun claimBarcodeNotFoundAlbums(limit: Int): Flux<String>
    fun findAlbumsWithBarcode(albumIds: Set<String>): Flux<AlbumBarcodes>
    fun findAlbumsForNameLookup(albumIds: Set<String>): Flux<AlbumNameLookup>
    fun saveReleaseGroupLookupResults(albums: List<AlbumReleaseGroupLookupResult>): Mono<Void>
}
