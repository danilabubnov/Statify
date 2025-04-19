package org.danila.model.spotify.track

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_favorite_tracks")
data class UserFavoriteTrack(

    @Column("user_id")
    val userId: UUID,

    @Column("track_id")
    val trackId: String,

    @Column("added_at")
    val addedAt: Instant

)