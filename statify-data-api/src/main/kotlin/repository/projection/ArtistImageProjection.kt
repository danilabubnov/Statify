package org.danila.repository.projection

interface ArtistImageProjection {
    fun getArtistId(): String
    fun getImageUrl(): String
    fun getImageHeight(): Int
    fun getImageWidth(): Int
    fun getImageOrder(): Int
}