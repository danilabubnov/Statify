package org.danila.dto.artist

import com.fasterxml.jackson.annotation.JsonProperty

data class FullArtistsResponseDTO(

    @JsonProperty("artists")
    val artists: List<ArtistDTO>

)