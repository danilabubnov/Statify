package org.danila.repository.projection

interface TrackArtistRow {
    fun getTrackId(): String
    fun getArtistId(): String
    fun getName(): String
    fun getPopularity(): Int?
    fun getFollowersTotal(): Int?
}