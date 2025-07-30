package org.danila.web.controller.graphql.artist

import com.netflix.graphql.dgs.DgsComponent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.danila.generated.types.AlbumSimple
import org.danila.generated.types.ArtistDTO
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.TrackSimple
import org.danila.mapper.graphql.toAlbumSimple
import org.danila.mapper.graphql.toArtistDTO
import org.danila.mapper.graphql.toArtistSimple
import org.danila.mapper.graphql.toTrackSimple
import org.danila.services.model.ArtistService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument

@DgsComponent
class ArtistGraphQLEndpoint(
    private val artistService: ArtistService
) {

    @DgsQuery
    fun topArtistsByPopularity(
        @InputArgument @Min(0) page: Int = 0,
        @InputArgument @Min(1) @Max(100) size: Int = 50
    ): List<ArtistSimple> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularity"))

        return artistService.findTop(pageable).map { it.toArtistSimple() }
    }

    @DgsQuery
    fun topArtistsByFollowers(
        @InputArgument @Min(0) page: Int = 0,
        @InputArgument @Min(1) @Max(100) size: Int = 50
    ): List<ArtistSimple> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "followersTotal"))

        return artistService.findTop(pageable).map { it.toArtistSimple() }
    }

    @DgsQuery
    fun artist(@InputArgument id: String): ArtistDTO =
        artistService.findById(id).toArtistDTO(
            topTracks = artistService.findTopTracksByArtist(id).map { it.toTrackSimple() },
            albums = artistService.findAlbumsByArtist(id).map { it.toAlbumSimple() }
        )

    @DgsQuery
    fun artistAlbums(@InputArgument id: String): List<AlbumSimple> =
        artistService.findAlbumsByArtist(id).map { it.toAlbumSimple() }

    @DgsQuery
    fun artistTopTracks(@InputArgument id: String): List<TrackSimple> =
        artistService.findTopTracksByArtist(id).map { it.toTrackSimple() }

}