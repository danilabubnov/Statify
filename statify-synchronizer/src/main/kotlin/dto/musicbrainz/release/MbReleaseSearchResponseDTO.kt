package org.danila.dto.musicbrainz.release

import com.fasterxml.jackson.annotation.JsonProperty
import org.danila.dto.musicbrainz.releasegroup.MbReleaseGroupDTO

data class MbReleaseSearchResponseDTO(

    @JsonProperty("releases")
    val releaseList: List<MbReleaseDTO>

)

data class MbReleaseDTO(

    @JsonProperty("release-group")
    val releaseGroup: MbReleaseGroupDTO

)

