package org.danila.model.spotify.track

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "user_favorite_tracks",
    indexes = [
        Index(name = "idx_user_favorite_tracks_user", columnList = "user_id"),
        Index(name = "idx_user_favorite_tracks_track", columnList = "track_id"),
        Index(name = "idx_user_favorite_tracks_user_added_track", columnList = "user_id, added_at, track_id")
    ]
)
data class UserFavoriteTrack(

    @EmbeddedId
    val id: UserFavoriteTrackId,

    @MapsId("trackId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    val track: Track,

    @Column(name = "added_at", nullable = false)
    val addedAt: Instant

) {

    constructor(
        userId: UUID,
        track: Track,
        addedAt: Instant = Instant.now(),
    ) : this(
        id = UserFavoriteTrackId(userId = userId, trackId = track.spotifyId),
        track = track,
        addedAt = addedAt
    )

}
