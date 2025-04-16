package org.danila.model.spotify.artist

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "artist_genres")
data class ArtistGenre(

    @Id
    @Column(value = "artist_id")
    var artistId: String,

    @Column(value = "genre")
    var genre: String

)