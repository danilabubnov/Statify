package org.danila.dto.common

import com.fasterxml.jackson.annotation.JsonProperty

data class ImageDTO(

    @JsonProperty("url")
    val url: String,

    @JsonProperty("height")
    val height: Int,

    @JsonProperty("width")
    val width: Int

)

data class FollowersDTO(

    @JsonProperty("total")
    val total: Int

)