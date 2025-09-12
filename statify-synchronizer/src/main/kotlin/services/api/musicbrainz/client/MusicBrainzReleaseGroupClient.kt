package org.danila.services.api.musicbrainz.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.logger
import org.danila.dto.musicbrainz.releasegroup.AlbumReleaseGroupLookupResult
import org.danila.event.scheduled.albums.LookupType
import org.danila.repository.projection.album.AlbumBarcodes
import org.danila.repository.projection.album.AlbumNameLookup
import org.danila.services.api.musicbrainz.retry.MusicBrainzRateLimitHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MusicBrainzReleaseGroupClient @Autowired constructor(
    private val musicBrainzAPI: MusicBrainzAPI,
    private val musicBrainzRateLimitHelper: MusicBrainzRateLimitHelper
) {

    private val logger by logger()

    suspend fun resolveByBarcode(albums: List<AlbumBarcodes>): List<AlbumReleaseGroupLookupResult> {
        logger.debug { "MusicBrainz resolve by barcode: start, albums=${albums.size}" }

        val releaseGroupAlbums = albums.map { album ->
            fetchByBarcode(album)
        }

        logger.debug { "MusicBrainz resolve by barcode: completed, resolved=${releaseGroupAlbums.size}" }

        return releaseGroupAlbums
    }

    suspend fun fetchByBarcode(album: AlbumBarcodes): AlbumReleaseGroupLookupResult {
        val barcodeQuery = when {
            album.ean != null && album.upc != null -> "barcode:(${album.ean} OR ${album.upc})"
            album.ean != null -> "barcode:${album.ean}"
            album.upc != null -> "barcode:${album.upc}"
            else -> {
                logger.debug { "No barcode for album=${album.spotifyId}, skipping lookup" }
                return AlbumReleaseGroupLookupResult(spotifyId = album.spotifyId, releaseGroupId = null, lookupType = LookupType.BY_BARCODE)
            }
        }

        val response = withContext(Dispatchers.IO) {
            logger.debug { "Executing HTTP request fetchByBarcode with album=$album" }
            musicBrainzRateLimitHelper.withMusicBrainzRateLimit {
                musicBrainzAPI.findRelease(query = barcodeQuery)
            }
        }

        val releaseGroupId = response.releaseList.firstOrNull()?.releaseGroup?.id

        logger.debug {
            "MB lookup album=${album.spotifyId}: used=$barcodeQuery, found=${releaseGroupId != null}"
        }

        return AlbumReleaseGroupLookupResult(
            spotifyId = album.spotifyId,
            releaseGroupId = releaseGroupId,
            lookupType = LookupType.BY_BARCODE
        )
    }

    suspend fun resolveByName(albums: List<AlbumNameLookup>): List<AlbumReleaseGroupLookupResult> {
        logger.debug { "MusicBrainz resolve by name: start, albums=${albums.size}" }

        val releaseGroupAlbums = albums.map { album ->
            fetchByName(album)
        }

        logger.debug { "MusicBrainz resolve by name: completed, resolved=${releaseGroupAlbums.size}" }

        return releaseGroupAlbums
    }

    suspend fun fetchByName(album: AlbumNameLookup): AlbumReleaseGroupLookupResult {
        val query = """releasegroup:"${album.name}" ${album.artists.joinToString(" OR ") { """artist:"$it"""" }}"""

        val response = withContext(Dispatchers.IO) {
            logger.debug { "Executing HTTP request fetchByName with album=$album" }
            musicBrainzRateLimitHelper.withMusicBrainzRateLimit {
                musicBrainzAPI.findReleaseGroup(query = query)
            }
        }

        val releaseGroupId = response.releaseGroups.firstOrNull()?.id

        logger.debug {
            "MB lookup album=${album.spotifyId}: used=$query, found=${releaseGroupId != null}"
        }

        return AlbumReleaseGroupLookupResult(
            spotifyId = album.spotifyId,
            releaseGroupId = releaseGroupId,
            lookupType = LookupType.BY_NAME
        )
    }

}

