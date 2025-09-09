package org.danila.dto.musicbrainz.release

import com.fasterxml.jackson.annotation.JsonProperty

data class ReleaseSearchResponseDTO(

    @JsonProperty("count")
    val count: Int,

    @JsonProperty("releases")
    val releaseList: List<ReleaseDTO>

)

data class ReleaseDTO(

    @JsonProperty("release-group")
    val releaseGroup: ReleaseGroupDTO

)

data class ReleaseGroupDTO(

    @JsonProperty("id")
    val id: String

)

