package org.danila.dto.album

import com.fasterxml.jackson.annotation.JsonProperty

data class AlbumExternalIds(

    @JsonProperty("ean")
    val ean: String?,

    @JsonProperty("upc")
    val upc: String?

)
