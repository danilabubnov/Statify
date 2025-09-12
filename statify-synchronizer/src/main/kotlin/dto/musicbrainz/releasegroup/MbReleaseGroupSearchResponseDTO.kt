package org.danila.dto.musicbrainz.releasegroup

import com.fasterxml.jackson.annotation.JsonProperty

data class MbReleaseGroupSearchResponseDTO(

    @JsonProperty("release-groups")
    val releaseGroups: List<MbReleaseGroupDTO>

)

data class MbReleaseGroupDTO(

    @JsonProperty("id")
    val id: String

)