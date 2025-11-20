package org.danila.repository

import org.danila.model.spotify.track.UserFavoriteTrack
import org.danila.model.spotify.track.UserFavoriteTrackId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface UserFavoriteTrackRepository : JpaRepository<UserFavoriteTrack, UserFavoriteTrackId> {

    fun findByIdUserId(
        userId: UUID,
        pageable: Pageable
    ): Slice<UserFavoriteTrack>

    fun findByIdUserIdAndAddedAtLessThanOrAddedAtEqualsAndIdTrackIdLessThan(
        userId: UUID,
        addedAt: Instant,
        sameAddedAt: Instant,
        trackId: String,
        pageable: Pageable
    ): Slice<UserFavoriteTrack>

    fun countByIdUserId(userId: UUID): Long

}