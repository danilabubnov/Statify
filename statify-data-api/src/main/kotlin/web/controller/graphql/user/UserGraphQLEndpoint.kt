package org.danila.web.controller.graphql.user

import com.netflix.graphql.dgs.DgsComponent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.danila.generated.types.*
import org.danila.services.model.user.UserService
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import java.util.*

@DgsComponent
class UserGraphQLEndpoint(
    private val userService: UserService
) {

    @DgsQuery
    fun getFavoriteTracks(
        @InputArgument userId: UUID,
        @InputArgument @Min(1) @Max(50) size: Int = 10,
        @InputArgument after: String?
    ): FavTrackConnection {
        val (tracks, pageInfoDto) = userService.getFavoriteTracks(userId = userId, size = size, after = after)

        return FavTrackConnection(
            edges = tracks.map { dto ->
                FavTrackEdge(
                    node = FavTrack(
                        id = dto.id,
                        durationMs = dto.durationMs,
                        explicit = dto.explicit,
                        name = dto.name,
                        popularity = dto.popularity,
                        trackNumber = dto.trackNumber,
                        albumId = dto.albumId,
                        addedAt = dto.addedAt
                    ),
                    cursor = dto.cursor
                )
            },
            pageInfo = PageInfo(
                hasNextPage = pageInfoDto.hasNextPage,
                hasPreviousPage = pageInfoDto.hasPreviousPage,
                startCursor = pageInfoDto.startCursor,
                endCursor = pageInfoDto.endCursor
            )
        )
    }

    @DgsQuery
    fun getFavoriteAlbums(
        @InputArgument userId: UUID,
        @InputArgument @Min(1) @Max(50) size: Int = 10,
        @InputArgument after: String?
    ): FavAlbumConnection {
        val (albums, pageInfoDto) = userService.getFavoriteAlbums(userId = userId, size = size, after = after)

        return FavAlbumConnection(
            edges = albums.map { dto ->
                FavAlbumEdge(
                    node = FavAlbum(
                        id = dto.id,
                        albumType = dto.albumType,
                        totalTracks = dto.totalTracks,
                        name = dto.name,
                        label = dto.label,
                        popularity = dto.popularity,
                        releaseDate = dto.releaseDate,
                        addedAt = dto.addedAt
                    ),
                    cursor = dto.cursor
                )
            },
            pageInfo = PageInfo(
                hasNextPage = pageInfoDto.hasNextPage,
                hasPreviousPage = pageInfoDto.hasPreviousPage,
                startCursor = pageInfoDto.startCursor,
                endCursor = pageInfoDto.endCursor
            )
        )
    }

}