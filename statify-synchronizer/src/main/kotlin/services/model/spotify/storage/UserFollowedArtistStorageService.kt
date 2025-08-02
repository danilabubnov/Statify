package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.artist.UserFollowedArtist
import org.danila.repository.UserFollowedArtistRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserFollowedArtistStorageService @Autowired constructor(
    private val userFollowedArtistRepository: UserFollowedArtistRepository,
    private val databaseExecutionContext: DatabaseExecutionContext
) {

    suspend fun findExistingUserFollowedArtists(userId: UUID): List<UserFollowedArtist> =
        databaseExecutionContext.withRead {
            userFollowedArtistRepository.findUserFollowedArtistsByUserId(userId).awaitList()
        }

    suspend fun persistUserFollowedArtists(userFollowedArtists: Collection<UserFollowedArtist>) {
        userFollowedArtists.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                userFollowedArtistRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}