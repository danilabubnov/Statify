package org.danila.repository

import org.danila.model.spotify.album.UserAlbumFavoriteId
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface UserFavoriteAlbumRepository : JpaRepository<UserFavoriteAlbum, UserAlbumFavoriteId> {

    fun findByIdUserId(
        userId: UUID,
        pageable: Pageable
    ): Slice<UserFavoriteAlbum>

    fun findByIdUserIdAndAddedAtLessThanOrAddedAtEqualsAndIdAlbumIdLessThan(
        userId: UUID,
        addedAt: Instant,
        sameAddedAt: Instant,
        albumId: String,
        pageable: Pageable
    ): Slice<UserFavoriteAlbum>

}
