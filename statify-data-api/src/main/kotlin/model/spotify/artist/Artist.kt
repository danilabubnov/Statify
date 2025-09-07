package org.danila.model.spotify.artist

import jakarta.persistence.*
import org.danila.model.spotify.Image
import org.danila.model.spotify.track.Track

@Entity
@Table(name = "artists", indexes = [Index(name = "idx_artists_name", columnList = "name")])
data class Artist(

    @Id
    @Column(name = "spotify_id", length = 50, nullable = false)
    val spotifyId: String,

    @Column(name = "followers_total")
    val followersTotal: Int?,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "artist_genres",
        joinColumns = [JoinColumn(name = "artist_id")],
        uniqueConstraints = [
            UniqueConstraint(
                name = "uk_artist_genre",
                columnNames = ["artist_id", "genre"]
            )
        ]
    )
    @Column(name = "genre", length = 100, nullable = false)
    val genres: List<String> = emptyList(),

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "artist_images", joinColumns = [JoinColumn(name = "artist_id")])
    @OrderColumn(name = "image_order")
    val images: List<Image> = emptyList(),

    @Column(name = "name", length = 255, nullable = false)
    val name: String,

    @Column(name = "popularity")
    val popularity: Int?,

    @ManyToMany(mappedBy = "artists", fetch = FetchType.LAZY)
    val tracks: Set<Track> = emptySet()

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Artist) return false
        return spotifyId == other.spotifyId
    }

    override fun hashCode(): Int = spotifyId.hashCode()

    override fun toString(): String = buildString {
        append("Artist(")
        append("spotifyId=").append(spotifyId)
        append(", name=").append(name)
        append(", followersTotal=").append(followersTotal)
        append(", popularity=").append(popularity)
        append(")")
    }

}