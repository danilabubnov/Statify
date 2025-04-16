package org.danila.model.spotify.track

import org.danila.dto.track.TrackDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "tracks")
data class Track(

    @Id
    @Column(value = "spotify_id")
    var spotifyId: String,

    @Column(value = "duration_ms")
    var durationMs: Int,

    @Column(value = "explicit")
    var explicit: Boolean,

    @Column(value = "name")
    var name: String,

    @Column(value = "popularity")
    var popularity: Int?,

    @Column(value = "track_number")
    var trackNumber: Int,

    @Column(value = "album_id")
    var albumId: String

) {

    fun isSimpleTrack() = this.popularity == null
    fun matchesDto(dto: TrackDTO) = this.popularity == dto.popularity

}