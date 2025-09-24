package org.danila.repository.projection

interface TrackImageProjection {
    fun getTrackId(): String
    fun getImageUrl(): String
    fun getImageHeight(): Int
    fun getImageWidth(): Int
    fun getImageOrder(): Int
}