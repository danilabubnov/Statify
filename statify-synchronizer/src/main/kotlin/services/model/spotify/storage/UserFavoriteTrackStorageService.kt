package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.repository.UserFavoriteTrackRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserFavoriteTrackStorageService @Autowired constructor(
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