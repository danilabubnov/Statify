package org.danila.web.controller.graphql.artist

import com.netflix.graphql.dgs.DgsComponent
import org.danila.generated.types.AlbumSimple
import org.danila.generated.types.ArtistDTO
import org.danila.generated.types.ArtistSimple
import org.danila.generated.types.TrackSimple
import org.danila.mapper.graphql.toAlbumSimple
import org.danila.mapper.graphql.toArtistDTO
import org.danila.mapper.graphql.toArtistSimple
import org.danila.mapper.graphql.toTrackSimple
import org.danila.services.model.artist.ArtistService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.danila.generated.types.ArtistPreview
import org.danila.mapper.graphql.toArtistPreview

@DgsComponent
class ArtistGraphQLEndpoint(
    private val artistService: ArtistService
) {

    @DgsQuery
    fun topArtistsByPopularity(
        @InputArgument page: Int,
        @InputArgument size: Int
    ): List<ArtistPreview> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularity"))

        return artistService.findTop(pageable).map { it.toArtistPreview() }
    }

    @DgsQuery
    fun topArtistsByFollowers(
        @InputArgument page: Int,
        @InputArgument size: Int
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