package org.danila.model.spotify.artist

import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "user_followed_artists",
    indexes = [
        Index(name = "idx_user_followed_artists_user", columnList = "user_id"),
        Index(name = "idx_user_followed_artists_artist", columnList = "artist_id"),
        Index(name = "idx_user_followed_artists_user_artist", columnList = "user_id, artist_id")
    ]
)
data class UserFollowedArtist(

    @EmbeddedId
    val id: UserFollowedArtistId,

    @MapsId("artistId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: Artist

) {

    constructor(
        userId: UUID,
        artist: Artist
    ): this(
        id = UserFollowedArtistId(userId = userId, artistId = artist.spotifyId),
        artist = artist
    )

}