package org.danila.repository

import event.UserLibraryStatus
import org.danila.model.spotify.UserSpotifyLibrary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface UserSpotifyLibraryRepository : JpaRepository<UserSpotifyLibrary, Long> {

    @Modifying
    @Query("UPDATE UserSpotifyLibrary l SET l.status = :status, l.lastSynchronizedAt = :lastSynchronizedAt WHERE l.id = :id")
    fun updateStatusById(@Param("id") id: UUID, @Param("status") status: UserLibraryStatus, @Param("lastSynchronizedAt") lastSynchronizedAt: Instant): Int

}