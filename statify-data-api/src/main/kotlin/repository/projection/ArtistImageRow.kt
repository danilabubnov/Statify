package org.danila.repository.projection

interface ArtistImageRow {
    fun getArtistId(): String
    fun getImageUrl(): String
    fun getImageHeight(): Int
    fun getImageWidth(): Int
    fun getImageOrder(): Int
}