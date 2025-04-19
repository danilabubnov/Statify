package org.danila.model.spotify.album

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_favorite_albums")
data class UserFavoriteAlbum(

    @Column("user_id")
    val userId: UUID,

    @Column("album_id")
    val albumId: String,

    @Column("added_at")
    val addedAt: Instant

)
