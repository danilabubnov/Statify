package org.danila.services.model.spotify

import kotlinx.coroutines.reactive.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.repository.UserFavoriteTrackRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class UserFavoriteTrackService @Autowired constructor(
    private val userFavoriteTrackRepository: UserFavoriteTrackRepository,
    private val transactionalOperator: TransactionalOperator,
) {

    suspend fun findExistingUserFavoriteTracks(userId: UUID): List<UserFavoriteTrack> = userFavoriteTrackRepository.findUserFavoriteTracksByUserId(userId).awaitList()

    suspend fun persistUserFavoriteTracks(userFavoriteTracks: Collection<UserFavoriteTrack>): Collection<UserFavoriteTrack> =
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