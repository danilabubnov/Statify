package org.danila.services.musicbrainz

import org.danila.services.api.musicbrainz.client.MusicBrainzBarcodeClient
import org.danila.services.model.spotify.storage.AlbumStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MusicBrainzService @Autowired constructor(
    private val albumStorageService: AlbumStorageService,
    private val musicBrainzBarcodeClient: MusicBrainzBarcodeClient
) {

    suspend fun resolveReleaseGroupsForAlbums(albumIds: List<String>) {
        val albumsWithBarcode = albumStorageService.findAlbumsWithBarcode(albumIds.toSet())

        val albumsWithReleaseGroup = musicBrainzBarcodeClient.resolveReleaseGroupsForAlbums(albumsWithBarcode)

        albumStorageService.persistReleaseGroupForAlbums(albumsWithReleaseGroup)
    }

}