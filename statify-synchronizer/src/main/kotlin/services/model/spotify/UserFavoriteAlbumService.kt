package org.danila.services.model.spotify

import kotlinx.coroutines.reactive.awaitSingle
import org.danila.awaitList
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.repository.UserFavoriteAlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class UserFavoriteAlbumService @Autowired constructor(
    private val userFavoriteAlbumRepository: UserFavoriteAlbumRepository,
    private val transactionalOperator: TransactionalOperator,
) {

    suspend fun findExistingUserFavoriteAlbums(userId: UUID): List<UserFavoriteAlbum> = userFavoriteAlbumRepository.findUserFavoriteAlbumsByUserId(userId).awaitList()

    suspend fun persistUserFavoriteAlbums(userFavoriteAlbums: Collection<UserFavoriteAlbum>): Collection<UserFavoriteAlbum> =
        transactionalOperator.executeAndAwait {
            userFavoriteAlbums.chunked(300)
                .map { chunk ->
                    userFavoriteAlbumRepository.insertBatch(chunk)
                        .collectList()
                        .awaitSingle()
                }
                .flatten()
        }

}