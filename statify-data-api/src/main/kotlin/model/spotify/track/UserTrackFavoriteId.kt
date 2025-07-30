package org.danila.model.spotify.track

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class UserTrackFavoriteId(

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "track_id", nullable = false)
    val trackId: String

)
