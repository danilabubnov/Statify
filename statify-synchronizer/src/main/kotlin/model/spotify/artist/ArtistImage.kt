package org.danila.model.spotify.artist

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "artist_images")
data class ArtistImage(

    @Id
    @Column(value = "artist_id")
    var artistId: String,

    @Column(value = "image_order")
    var imageOrder: Int,

    @Column(value = "image_url")
    var imageUrl: String,

    @Column(value = "image_height")
    var imageHeight: Int,

    @Column(value = "image_width")
    var imageWidth: Int

)