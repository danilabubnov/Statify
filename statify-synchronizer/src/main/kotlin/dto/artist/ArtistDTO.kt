package org.danila.dto.artist

import com.fasterxml.jackson.annotation.JsonProperty
import org.danila.dto.common.FollowersDTO
import org.danila.dto.common.ImageDTO

data class ArtistDTO(

    @JsonProperty("followers")
    val followers: FollowersDTO,

    @JsonProperty("genres")
    val genres: List<String>,

    @JsonProperty("id")
    val id: String,

    @JsonProperty("images")
    val images: List<ImageDTO>,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("popularity")
    val popularity: Int
)

data class ArtistSimpleDTO(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String

)

data class CursorsDTO(

    @JsonProperty("after")
    val after: String?,

    @JsonProperty("before")
    val before: String?

)

data class FollowingArtistsDTO(

    @JsonProperty("limit")
    val limit: Int,

    @JsonProperty("next")
    val next: String?,

    @JsonProperty("cursors")
    val cursors: CursorsDTO,

    @JsonProperty("total")
    val total: Int,

    @JsonProperty("items")
    val items: List<ArtistDTO>

)

data class FollowingArtistsResponseDTO(

    @JsonProperty("artists")
    val artists: FollowingArtistsDTO

)

data class ArtistsDTO(

    @JsonProperty("artists")
    val artists: List<ArtistDTO>

)