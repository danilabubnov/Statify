package org.danila.model.spotify.album

import org.danila.dto.album.AlbumDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "albums")
data class Album(

    @Id
    @Column(value = "spotify_id")
    var spotifyId: String,

    @Column(value = "album_type")
    var albumType: String,

    @Column(value = "total_tracks")
    var totalTracks: Int,

    @Column(value = "name")
    var name: String,

    @Column(value = "label")
    var label: String?,

    @Column(value = "popularity")
    var popularity: Int?,

    @Column(value = "release_date_raw")
    var releaseDateRaw: String,

    @Column(value = "release_date_precision")
    var releaseDatePrecision: String,

    @Column(value = "release_year")
    var releaseYear: Int,

    @Column(value = "release_month")
    var releaseMonth: Int?,

    @Column(value = "release_day")
    var releaseDay: Int?

) {

    fun isSimpleAlbum() = this.label == null && this.popularity == null
    fun matchesDto(dto: AlbumDTO) = this.label == dto.label && this.popularity == dto.popularity && this.totalTracks == dto.totalTracks && this.releaseDateRaw == dto.releaseDate

}