package org.danila.services.api.musicbrainz.client

import org.danila.dto.musicbrainz.release.MbReleaseSearchResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicBrainzAPI {

    /**
     * @param query
     *   The search query. For UPC/EAN it must always be in the format: "barcode:<code>".
     *   Examples:
     *     - "barcode:602537479870"
     *     - "barcode:0123456789012"
     * @param fmt
     *   Response format. Default is "json".
     *   Possible values: "json", "xml".
     */
    @GET("ws/2/release")
    suspend fun findRelease(
        @Query("query") query: String,
        @Query("fmt") fmt: String = "json",
        @Query("limit") limit: Int = 1
    ): MbReleaseSearchResponseDTO

}