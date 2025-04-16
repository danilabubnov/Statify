package org.danila.dto.track

import com.fasterxml.jackson.annotation.JsonProperty

data class FullTracksResponseDTO(

    @JsonProperty("tracks")
    val tracks: List<TrackDTO>

)