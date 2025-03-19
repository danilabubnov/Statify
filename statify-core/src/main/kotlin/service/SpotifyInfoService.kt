package org.danila.service

import jakarta.transaction.Transactional
import org.danila.model.spotify.SpotifyInfo
import org.danila.model.users.User
import org.danila.repository.SpotifyInfoRepository
import org.danila.service.utils.IdGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpotifyInfoService @Autowired constructor(
    private val spotifyInfoRepository: SpotifyInfoRepository,
    private val idGeneratorService: IdGeneratorService
) {

    fun findBySpotifyIdOrNull(spotifyId: String): SpotifyInfo? = spotifyInfoRepository.findBySpotifyId(spotifyId)

    @Transactional
    fun create(spotifyId: String, email: String, user: User?): SpotifyInfo {
        return spotifyInfoRepository.save(
            SpotifyInfo(
                id = idGeneratorService.uuid,
                spotifyId = spotifyId,
                email = email,
                user = user
            )
        )
    }

    @Transactional
    fun setUser(spotifyInfo: SpotifyInfo, user: User?): SpotifyInfo = spotifyInfoRepository.save(spotifyInfo.copy(user = user))

}