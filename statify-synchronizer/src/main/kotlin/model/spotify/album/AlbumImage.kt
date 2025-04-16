package org.danila.model.spotify.album

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "album_images")
data class AlbumImage(

    @Id
    @Column(value = "album_id")
    var albumId: String,

    @Column(value = "image_order")
    var imageOrder: Int,

    @Column(value = "image_url")
    var imageUrl: String,

    @Column(value = "image_height")
    var imageHeight: Int,

    @Column(value = "image_width")
    var imageWidth: Int

)