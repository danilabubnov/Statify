package org.danila.model.spotify

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Image(

    @Column(name = "image_url")
    var imageUrl: String,

    @Column(name = "image_height")
    var imageHeight: Int,

    @Column(name = "image_width")
    var imageWidth: Int

)