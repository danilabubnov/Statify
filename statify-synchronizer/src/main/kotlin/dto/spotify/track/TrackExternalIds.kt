package org.danila.dto.spotify.track

import com.fasterxml.jackson.annotation.JsonProperty

data class TrackExternalIds(

    @JsonProperty("isrc")
    val isrc: String?

)
