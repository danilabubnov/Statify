package org.danila.model.spotify.album

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class UserAlbumFavoriteId(

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "album_id", nullable = false)
    val albumId: String

)