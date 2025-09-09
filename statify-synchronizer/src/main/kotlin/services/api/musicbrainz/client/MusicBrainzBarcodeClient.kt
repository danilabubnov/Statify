package org.danila.services.api.musicbrainz.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.logger
import org.danila.dto.musicbrainz.release.AlbumReleaseGroupMapping
import org.danila.model.spotify.album.AlbumBarcodes
import org.danila.services.api.musicbrainz.retry.MusicBrainzRateLimitHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MusicBrainzBarcodeClient @Autowired constructor(
    private val musicBrainzAPI: MusicBrainzAPI,
    private val musicBrainzRateLimitHelper: MusicBrainzRateLimitHelper
) {

    private val logger by logger()

    suspend fun resolveReleaseGroupsForAlbums(albums: List<AlbumBarcodes>): List<AlbumReleaseGroupMapping> {
        logger.debug { "MusicBrainz resolve: start, albums=${albums.size}" }

        val releaseGroupAlbums = albums.map { album ->
            getReleaseGroupForAlbum(album)
        }

        logger.debug { "MusicBrainz resolve: completed, resolved=${releaseGroupAlbums.size}" }

        return releaseGroupAlbums
    }

    suspend fun getReleaseGroupForAlbum(album: AlbumBarcodes): AlbumReleaseGroupMapping {
        val barcodeQuery = when {
            album.ean != null && album.upc != null -> "barcode:(${album.ean} OR ${album.upc})"
            album.ean != null -> "barcode:${album.ean}"
            album.upc != null -> "barcode:${album.upc}"
            else -> {
                logger.debug { "No barcode for album=${album.spotifyId}, skipping lookup" }
                return AlbumReleaseGroupMapping(album.spotifyId, null)
            }
        }

        val response = withContext(Dispatchers.IO) {
            logger.debug { "Executing HTTP request getReleaseGroupAlbum with album=$album" }
            musicBrainzRateLimitHelper.withMusicBrainzRateLimit {
                musicBrainzAPI.findByBarcode(query = barcodeQuery)
            }
        }

        val releaseGroupId = response.releaseList.firstOrNull()?.releaseGroup?.id

        logger.debug {
            "MB lookup album=${album.spotifyId}: used=$barcodeQuery, found=${releaseGroupId != null}"
        }

        return AlbumReleaseGroupMapping(
            spotifyId = album.spotifyId,
            releaseGroupId = releaseGroupId
        )
    }

}

