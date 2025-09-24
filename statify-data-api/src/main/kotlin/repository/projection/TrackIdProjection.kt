package org.danila.repository.projection

interface TrackIdProjection {
    fun getSpotifyId(): String
    fun getName(): String
}