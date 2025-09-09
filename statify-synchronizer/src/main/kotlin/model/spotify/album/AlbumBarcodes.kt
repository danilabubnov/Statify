package org.danila.model.spotify.album

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "albums")
data class AlbumBarcodes(

    @Id
    @Column(value = "spotify_id")
    var spotifyId: String,

    @Column(value = "ean")
    var ean: String?,

    @Column(value = "upc")
    var upc: String?

)
