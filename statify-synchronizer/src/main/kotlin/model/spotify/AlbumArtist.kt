package org.danila.model.spotify

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "album_artists")
data class AlbumArtist(

    @Column(value = "album_id")
    var albumId: String,

    @Column(value = "artist_id")
    var artistId: String

)