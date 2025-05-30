package org.danila.services.model.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.danila.MAX_SAVED_ENTITIES_CHUNK_SIZE
import org.danila.awaitList
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.repository.UserFavoriteAlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class UserFavoriteAlbumService @Autowired constructor(
    @Qualifier("databaseWriteSemaphore") private val writeSemaphore: Semaphore,
    @Qualifier("databaseReadSemaphore") private val readSemaphore: Semaphore,

    private val userFavoriteAlbumRepository: UserFavoriteAlbumRepository,
    private val transactionalOperator: TransactionalOperator,
) {

    suspend fun findExistingUserFavoriteAlbums(userId: UUID): List<UserFavoriteAlbum> =
        withContext(Dispatchers.IO) {
            readSemaphore.withPermit { userFavoriteAlbumRepository.findUserFavoriteAlbumsByUserId(userId).awaitList() }
        }

    suspend fun persistUserFavoriteAlbums(userFavoriteAlbums: Collection<UserFavoriteAlbum>): Unit =
        withContext(Dispatchers.IO) {
            writeSemaphore.withPermit {
                userFavoriteAlbums.chunked(MAX_SAVED_ENTITIES_CHUNK_SIZE).forEach { chunk ->
                    transactionalOperator.executeAndAwait {
                        userFavoriteAlbumRepository.insertBatch(chunk)
                            .awaitSingleOrNull()
                    }
                }
            }
        }

}