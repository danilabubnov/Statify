package org.danila.dto.spotify.album

import com.fasterxml.jackson.annotation.JsonProperty

data class AlbumExternalIds(

    @JsonProperty("ean")
    val ean: String?,

    @JsonProperty("upc")
    val upc: String?

)
