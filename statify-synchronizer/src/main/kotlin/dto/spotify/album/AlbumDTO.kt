package org.danila.dto.spotify.album

import com.fasterxml.jackson.annotation.JsonProperty
import org.danila.dto.spotify.artist.ArtistSimpleDTO
import org.danila.dto.spotify.common.ImageDTO
import org.danila.dto.spotify.track.TracksDTO

data class AlbumDTO(

    @JsonProperty("album_type")
    val albumType: String,

    @JsonProperty("total_tracks")
    val totalTracks: Int,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("images")
    val images: List<ImageDTO>,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("release_date")
    val releaseDate: String,

    @JsonProperty("release_date_precision")
    val releaseDatePrecision: String,

    @JsonProperty("artists")
    val artists: List<ArtistSimpleDTO>,

    @JsonProperty("tracks")
    val tracks: TracksDTO,

    @JsonProperty("label")
    val label: String,

    @JsonProperty("popularity")
    val popularity: Int,

    @JsonProperty("external_ids")
    val externalIds: AlbumExternalIds

) {

    fun normalized(): AlbumDTO =
        this.copy(
            albumType = this.albumType.uppercase(),
            releaseDatePrecision = this.releaseDatePrecision.uppercase()
        )

}

data class AlbumSimpleDTO(

    @JsonProperty("album_type")
    val albumType: String,

    @JsonProperty("total_tracks")
    val totalTracks: Int,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("images")
    val images: List<ImageDTO>,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("release_date")
    val releaseDate: String,

    @JsonProperty("release_date_precision")
    val releaseDatePrecision: String,

    @JsonProperty("artists")
    val artists: List<ArtistSimpleDTO>

) {

    fun normalized(): AlbumSimpleDTO =
        this.copy(
            albumType = this.albumType.uppercase(),
            releaseDatePrecision = this.releaseDatePrecision.uppercase()
        )

}

data class SavedAlbumItemDTO(

    @JsonProperty("added_at")
    val addedAt: String,

    @JsonProperty("album")
    val album: AlbumDTO

)

data class SavedAlbumsResponseDTO(

    @JsonProperty("limit")
    val limit: Int,

    @JsonProperty("next")
    val next: String?,

    @JsonProperty("total")
    val total: Int,

    @JsonProperty("items")
    val items: List<SavedAlbumItemDTO>

)