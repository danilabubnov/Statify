package org.danila.repository.projection.album

data class AlbumNameLookup(
    val spotifyId: String,
    val name: String,
    val artists: List<ArtistName>
)

data class ArtistName(
    val name: String
)