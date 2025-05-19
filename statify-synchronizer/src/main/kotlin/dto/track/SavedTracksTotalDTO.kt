package org.danila.dto.track

import com.fasterxml.jackson.annotation.JsonProperty

data class SavedTracksTotalDTO(

    @JsonProperty("total")
    val total: Int

)