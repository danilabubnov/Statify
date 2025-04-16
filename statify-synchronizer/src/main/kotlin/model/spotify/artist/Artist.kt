package org.danila.model.spotify.artist

import org.danila.dto.artist.ArtistDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "artists")
data class Artist(

    @Id
    @Column(value = "spotify_id")
    var spotifyId: String,

    @Column(value = "followers_total")
    var followersTotal: Int? = null,

    @Column(value = "name")
    var name: String,

    @Column(value = "popularity")
    var popularity: Int? = null

) {

    fun isSimpleArtist() = this.popularity == null && this.followersTotal == null
    fun matchesDto(dto: ArtistDTO) = this.popularity == dto.popularity && this.followersTotal == dto.followers.total

}