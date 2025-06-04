package org.danila.services.model.spotify.storage

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.danila.configuration.constants.spotify.SpotifyBatchConfig.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.repository.UserFavoriteAlbumRepository
import org.danila.services.model.spotify.DatabaseExecutionContext
import org.danila.util.reactive.awaitList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserFavoriteAlbumStorageService @Autowired constructor(
    private val userFavoriteAlbumRepository: UserFavoriteAlbumRepository,
    private val databaseExecutionContext: DatabaseExecutionContext
) {

    suspend fun findExistingUserFavoriteAlbums(userId: UUID): List<UserFavoriteAlbum> =
        databaseExecutionContext.withRead {
            userFavoriteAlbumRepository.findUserFavoriteAlbumsByUserId(userId).awaitList()
        }

    suspend fun persistUserFavoriteAlbums(userFavoriteAlbums: Collection<UserFavoriteAlbum>) {
        userFavoriteAlbums.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
            databaseExecutionContext.withWriteTransactionRetry {
                userFavoriteAlbumRepository.insertBatch(chunk).awaitSingleOrNull()
            }
        }
    }

}