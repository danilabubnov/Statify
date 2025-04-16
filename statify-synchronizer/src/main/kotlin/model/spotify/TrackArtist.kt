package org.danila.model.spotify

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "track_artists")
data class TrackArtist(

    @Column(value = "track_id")
    var trackId: String,

    @Column(value = "artist_id")
    var artistId: String

)