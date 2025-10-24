package org.danila.service.model.spotify

import jakarta.transaction.Transactional
import org.danila.model.spotify.SpotifyInfo
import org.danila.model.users.User
import org.danila.repository.SpotifyInfoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyInfoService @Autowired constructor(
    private val spotifyInfoRepository: SpotifyInfoRepository
) {

    fun findBySpotifyIdOrNull(spotifyId: String): SpotifyInfo? = spotifyInfoRepository.findBySpotifyId(spotifyId)

    @Transactional
    fun create(spotifyId: String, email: String, user: User?, displayName: String? = null): SpotifyInfo {
        return spotifyInfoRepository.save(
            SpotifyInfo(
                spotifyId = spotifyId,
                email = email,
                displayName = displayName,
                user = user,
                refreshToken = null,
            )
        )
    }

    @Transactional
    fun update(spotifyInfo: SpotifyInfo): SpotifyInfo = spotifyInfoRepository.save(spotifyInfo)

}