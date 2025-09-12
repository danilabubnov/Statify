package org.danila.services.musicbrainz

import org.danila.services.api.musicbrainz.client.MusicBrainzReleaseGroupClient
import org.danila.services.model.spotify.storage.AlbumStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MusicBrainzService @Autowired constructor(
    private val albumStorageService: AlbumStorageService,
    private val musicBrainzReleaseGroupClient: MusicBrainzReleaseGroupClient
) {

    suspend fun resolveReleaseGroupsForAlbumsByBarcode(albumIds: List<String>) {
        val albumsWithBarcode = albumStorageService.findAlbumsWithBarcode(albumIds.toSet())

        val albumsWithReleaseGroup = musicBrainzReleaseGroupClient.resolveByBarcode(albumsWithBarcode)

        albumStorageService.persistReleaseGroupForAlbums(albumsWithReleaseGroup)
    }

    suspend fun resolveReleaseGroupsForAlbumsByName(albumIds: List<String>) {
        val albumProjections = albumStorageService.findAlbumsWithName(albumIds.toSet())

        val albumsWithReleaseGroup = musicBrainzReleaseGroupClient.resolveByName(albumProjections)

        albumStorageService.persistReleaseGroupForAlbums(albumsWithReleaseGroup)
    }

}