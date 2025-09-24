package org.danila.repository.projection

interface AlbumArtistRow {
    fun getAlbumId(): String
    fun getArtistId(): String
    fun getName(): String
    fun getPopularity(): Int?
    fun getFollowersTotal(): Int?
}