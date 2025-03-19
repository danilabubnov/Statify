package org.danila.repository

import org.danila.model.spotify.SpotifyInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpotifyInfoRepository : JpaRepository<SpotifyInfo, UUID> {

    fun findBySpotifyId(spotifyId: String): SpotifyInfo?

}