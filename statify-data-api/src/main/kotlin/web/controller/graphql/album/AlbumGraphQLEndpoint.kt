package org.danila.web.controller.graphql.album

import com.netflix.graphql.dgs.DgsComponent
import org.danila.generated.types.AlbumDTO
import org.danila.generated.types.AlbumSimple
import org.danila.generated.types.TrackSimple
import org.danila.mapper.graphql.toAlbumDTO
import org.danila.mapper.graphql.toAlbumSimple
import org.danila.mapper.graphql.toTrackSimple
import org.danila.model.spotify.album.AlbumType
import org.danila.services.model.AlbumService
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import java.time.Year

@DgsComponent
class AlbumGraphQLEndpoint(
    private val albumService: AlbumService
) {

    @DgsQuery
    fun topAlbumsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int,
        @InputArgument year: Int?,
        @InputArgument albumType: AlbumType
    ): List<AlbumSimple> {
        if (year != null) {
            val currentYear = Year.now().value

            require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }
        }

        return albumService
            .findTopByPopularity(page = page, pageSize = size, year = year, albumType = albumType)
            .map { it.toAlbumSimple() }
    }

    @DgsQuery
    fun album(@InputArgument id: String): AlbumDTO =
        albumService.findById(id).toAlbumDTO()

    @DgsQuery
    fun albumTracks(@InputArgument id: String): List<TrackSimple> =
        albumService.findTracksByAlbum(id).map { it.toTrackSimple() }

    @DgsQuery
    fun searchAlbums(
        @InputArgument query: String,
        @InputArgument page: Int,
        @InputArgument size: Int
    ): List<AlbumSimple> {
        val sanitized = query.trim().takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Query cannot be empty")

        return albumService
            .searchByName(query = sanitized, page = page, pageSize = size)
            .map { it.toAlbumSimple() }
    }

    @DgsQuery
    fun albumsByYear(@InputArgument year: Int): List<AlbumSimple> {
        val currentYear = Year.now().value

        require(year in 1900..currentYear) { "Year must be between 1900 and $currentYear" }

        return albumService.findByYear(year).map { it.toAlbumSimple() }
    }

}