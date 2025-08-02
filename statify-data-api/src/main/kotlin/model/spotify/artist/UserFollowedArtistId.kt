package org.danila.model.spotify.artist

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class UserFollowedArtistId(

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "artist_id", nullable = false)
    val artistId: String

)
