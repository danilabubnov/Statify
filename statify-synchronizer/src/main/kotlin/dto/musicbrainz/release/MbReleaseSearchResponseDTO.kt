package org.danila.dto.musicbrainz.release

import com.fasterxml.jackson.annotation.JsonProperty

data class MbReleaseSearchResponseDTO(

    @JsonProperty("releases")
    val releaseList: List<MbReleaseDTO>

)

data class MbReleaseDTO(

    @JsonProperty("title")
    val title: String,

    @JsonProperty("disambiguation")
    val disambiguation: String?,

    @JsonProperty("release-group")
    val releaseGroup: MbReleaseGroupDTO,

    @JsonProperty("artist-credit")
    val artists: List<MbArtistCreditDTO>

)

data class MbReleaseGroupDTO(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("title")
    val title: String? = null

)

data class MbArtistCreditDTO(

    @JsonProperty("name")
    val name: String

)