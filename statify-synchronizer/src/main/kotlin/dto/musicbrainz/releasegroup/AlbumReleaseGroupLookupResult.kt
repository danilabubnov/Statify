package org.danila.dto.musicbrainz.releasegroup

import org.danila.event.scheduled.albums.LookupType

data class AlbumReleaseGroupLookupResult(
    val spotifyId: String,
    val releaseGroupId: String?,
    val lookupType: LookupType
)