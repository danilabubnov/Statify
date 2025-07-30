package org.danila.model.spotify.album

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "user_favorite_albums",
    indexes = [
        Index(name = "idx_ufa_user", columnList = "user_id"),
        Index(name = "idx_ufa_album", columnList = "album_id"),
        Index(name = "idx_uft_user_added_album", columnList = "user_id, added_at, album_id")
    ]
)
data class UserFavoriteAlbum(

    @EmbeddedId
    val id: UserAlbumFavoriteId,

    @MapsId("albumId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    val album: Album,

    @Column(name = "added_at", nullable = false)
    val addedAt: Instant

) {

    constructor(
        userId: UUID,
        album: Album,
        addedAt: Instant = Instant.now(),
    ) : this(
        id = UserAlbumFavoriteId(userId = userId, albumId = album.spotifyId),
        album = album,
        addedAt = addedAt
    )

}
