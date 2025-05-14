package org.danila.services.model.spotify

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.danila.awaitList
import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.repository.UserFavoriteTrackRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class UserFavoriteTrackService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val userFavoriteTrackRepository: UserFavoriteTrackRepository,
    private val transactionalOperator: TransactionalOperator,
) {

    suspend fun findExistingUserFavoriteTracks(userId: UUID): List<UserFavoriteTrack> =
        writeSemaphore.withPermit { userFavoriteTrackRepository.findUserFavoriteTracksByUserId(userId).awaitList() }

    suspend fun persistUserFavoriteTracks(userFavoriteTracks: Collection<UserFavoriteTrack>): Collection<UserFavoriteTrack> =
        readSemaphore.withPermit {
            transactionalOperator.executeAndAwait {
                userFavoriteTracks.chunked(300)
                    .map { chunk ->
                        userFavoriteTrackRepository.insertBatch(chunk)
                            .collectList()
                            .awaitSingle()
                    }
                    .flatten()
            }
        }

}