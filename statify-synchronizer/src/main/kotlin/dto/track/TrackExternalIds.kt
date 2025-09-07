package org.danila.dto.track

import com.fasterxml.jackson.annotation.JsonProperty

data class TrackExternalIds(

    @JsonProperty("isrc")
    val isrc: String?

)
