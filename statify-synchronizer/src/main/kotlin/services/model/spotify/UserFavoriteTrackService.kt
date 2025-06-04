package org.danila.services.model.spotify

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.repository.UserFavoriteTrackRepository
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserFavoriteTrackService @Autowired constructor(
    private val userFavoriteTrackRepository: UserFavoriteTrackRepository,
    private val databaseExecutionContext: DatabaseExecutionContext,
) {

    suspend fun findExistingUserFavoriteTracks(userId: UUID): List<UserFavoriteTrack> =
        databaseExecutionContext.withRead {
            userFavoriteTrackRepository.findUserFavoriteTracksByUserId(userId).awaitList()
        }

    suspend fun persistUserFavoriteTracks(userFavoriteTracks: Collection<UserFavoriteTrack>) {
        userFavoriteTracks.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                userFavoriteTrackRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}