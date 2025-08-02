package org.danila.model.spotify.artist

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_followed_artists")
data class UserFollowedArtist(

    @Column("user_id")
    val userId: UUID,

    @Column("artist_id")
    val artistId: String

)
