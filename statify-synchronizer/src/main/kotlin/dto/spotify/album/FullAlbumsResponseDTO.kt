package org.danila.dto.spotify.album

import com.fasterxml.jackson.annotation.JsonProperty

data class FullAlbumsResponseDTO(

    @JsonProperty("albums")
    val albums: List<AlbumDTO>

)