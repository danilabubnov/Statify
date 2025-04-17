package org.danila.dto.track

import com.fasterxml.jackson.annotation.JsonProperty
import org.danila.dto.album.AlbumSimpleDTO
import org.danila.dto.artist.ArtistSimpleDTO

data class TrackDTO(

    @JsonProperty("album")
    val album: AlbumSimpleDTO,

    @JsonProperty("artists")
    val artists: List<ArtistSimpleDTO>,

    @JsonProperty("duration_ms")
    val durationMs: Int,

    @JsonProperty("explicit")
    val explicit: Boolean,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("popularity")
    val popularity: Int,

    @JsonProperty("track_number")
    val trackNumber: Int

)

data class TrackItemDTO(

    @JsonProperty("artists")
    val artists: List<ArtistSimpleDTO>,

    @JsonProperty("duration_ms")
    val durationMs: Int,

    @JsonProperty("explicit")
    val explicit: Boolean,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("track_number")
    val trackNumber: Int

)

data class TracksDTO(

    @JsonProperty("items")
    val items: List<TrackItemDTO>

)

data class SavedTrackItemDTO(

    @JsonProperty("added_at")
    val addedAt: String,

    @JsonProperty("track")
    val track: TrackDTO

)

data class SavedTracksResponseDTO(

    @JsonProperty("limit")
    val limit: Int,

    @JsonProperty("next")
    val next: String?,

    @JsonProperty("items")
    val items: List<SavedTrackItemDTO>

)