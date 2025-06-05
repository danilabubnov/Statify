package org.danila.service.model.spotify

import event.UserLibraryStatus
import jakarta.transaction.Transactional
import org.danila.model.spotify.UserSpotifyLibrary
import org.danila.model.users.User
import org.danila.repository.UserSpotifyLibraryRepository
import org.danila.service.utils.IdGeneratorService
import org.danila.service.utils.InstantService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserSpotifyLibraryService @Autowired constructor(
    private val userSpotifyLibraryRepository: UserSpotifyLibraryRepository,
    private val idGeneratorService: IdGeneratorService,
    private val instantService: InstantService
) {

    @Transactional
    fun create(user: User): UserSpotifyLibrary {
        return userSpotifyLibraryRepository.save(
            UserSpotifyLibrary(
                id = idGeneratorService.uuid,
                user = user,
                lastSynchronizedAt = null,
                status = UserLibraryStatus.PENDING
            )
        )
    }

    @Transactional
    fun updateStatus(id: UUID, status: UserLibraryStatus) {
        userSpotifyLibraryRepository.updateStatusById(id = id, status = status, lastSynchronizedAt = instantService.now)
    }

}