package org.danila.model.spotify.track

import jakarta.persistence.*
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.artist.Artist

@Entity
@Table(name = "tracks", indexes = [Index(name = "idx_tracks_name", columnList = "name")])
data class Track(

    @Id
    @Column(name = "spotify_id", length = 50, nullable = false)
    var spotifyId: String,

    @Column(name = "duration_ms", nullable = false)
    var durationMs: Int,

    @Column(name = "explicit", nullable = false)
    var explicit: Boolean,

    @Column(name = "name", length = 255, nullable = false)
    var name: String,

    @Column(name = "popularity")
    var popularity: Int?,

    @Column(name = "track_number", nullable = false)
    var trackNumber: Int,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    var album: Album,

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "track_artists",
        joinColumns = [JoinColumn(name = "track_id")],
        inverseJoinColumns = [JoinColumn(name = "artist_id")]
    )
    var artists: Set<Artist> = emptySet()

)