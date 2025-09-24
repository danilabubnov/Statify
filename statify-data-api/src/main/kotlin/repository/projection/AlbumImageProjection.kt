package org.danila.repository.projection

interface AlbumImageProjection {
    fun getAlbumId(): String
    fun getImageUrl(): String
    fun getImageHeight(): Int
    fun getImageWidth(): Int
    fun getImageOrder(): Int
}