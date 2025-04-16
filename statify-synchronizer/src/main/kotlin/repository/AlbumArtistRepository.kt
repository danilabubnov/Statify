package org.danila.repository

import org.danila.model.spotify.AlbumArtist
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AlbumArtistRepository {

    fun insert(albumArtist: AlbumArtist): Mono<AlbumArtist>

    fun findByAlbumArtistPairs(pairs: Set<Pair<String, String>>): Flux<AlbumArtist>

    fun insertBatch(albumArtists: Collection<AlbumArtist>): Flux<AlbumArtist>

}