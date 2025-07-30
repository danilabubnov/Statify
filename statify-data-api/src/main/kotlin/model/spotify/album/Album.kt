package org.danila.model.spotify.album

import jakarta.persistence.*
import org.danila.model.spotify.Image
import org.danila.model.spotify.artist.Artist
import org.danila.model.spotify.track.Track

@Entity
@Table(
    name = "albums",
    indexes = [
        Index(name = "idx_release_year", columnList = "release_year"),
        Index(name = "idx_release_month", columnList = "release_month"),
        Index(name = "idx_album_name", columnList = "name")
    ]
)
data class Album(

    @Id
    @Column(name = "spotify_id", length = 50, nullable = false)
    val spotifyId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type", nullable = false, length = 32)
    val albumType: AlbumType,

    @Column(name = "total_tracks", nullable = false)
    var totalTracks: Int,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "album_images", joinColumns = [JoinColumn(name = "album_id")])
    @OrderColumn(name = "image_order")
    var images: List<Image>,

    @Column(name = "name", length = 255, nullable = false)
    var name: String,

    @Embedded
    var releaseDate: AlbumReleaseDate,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "album_artists",
        joinColumns = [JoinColumn(name = "album_id")],
        inverseJoinColumns = [JoinColumn(name = "artist_id")]
    )
    val artists: Set<Artist>,

    @Column(name = "label")
    var label: String?,

    @Column(name = "popularity")
    var popularity: Int?,

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val tracks: List<Track> = emptyList()

)