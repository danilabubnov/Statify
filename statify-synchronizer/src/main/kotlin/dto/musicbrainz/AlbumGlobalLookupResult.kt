package org.danila.dto.musicbrainz

import org.danila.dto.musicbrainz.release.MbReleaseSearchResponseDTO
import org.danila.event.scheduled.albums.LookupType
import org.danila.repository.projection.album.ArtistName

data class AlbumGlobalLookupResult(
    val spotifyId: String,
    val name: String,
    val artists: List<ArtistName>,
    val lookupResult: MbReleaseSearchResponseDTO,
    val lookupType: LookupType
)
