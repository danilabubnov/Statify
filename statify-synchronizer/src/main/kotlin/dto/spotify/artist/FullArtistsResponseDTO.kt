package org.danila.dto.spotify.artist

import com.fasterxml.jackson.annotation.JsonProperty

data class FullArtistsResponseDTO(

    @JsonProperty("artists")
    val artists: List<ArtistDTO>

)