package org.danila.services.musicbrainz

import org.danila.dto.musicbrainz.releasegroup.AlbumReleaseGroupLookupResult
import org.danila.event.scheduled.albums.LookupType
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
        val albumsWithLookupResults = musicBrainzReleaseGroupClient.resolveByName(albumProjections)

        fun String.normalized() = lowercase()
            .replace("[^a-z0-9]+".toRegex(), " ")
            .trim()

        fun String.takePercent(percent: Int): String {
            require(percent in 0..100)

            return take(maxOf(1, (length * percent / 100.0).toInt()))
        }

        val matched = albumsWithLookupResults.map { albumResult ->
            val albumNamePart = albumResult.name.normalized().takePercent(50)
            val albumArtists = albumResult.artists.map { it.name.lowercase() }.toSet()

            val release = albumResult.lookupResult.releaseList
                .firstOrNull { release ->
                    val releaseArtists = release.artists.map { it.name.lowercase() }.toSet()

                    releaseArtists.intersect(albumArtists).isNotEmpty() &&
                            release.title.normalized().startsWith(albumNamePart)
                }

            AlbumReleaseGroupLookupResult(
                spotifyId = albumResult.spotifyId,
                releaseGroupId = release?.releaseGroup?.id,
                lookupType = LookupType.BY_NAME
            )
        }

        albumStorageService.persistReleaseGroupForAlbums(matched)
    }

}