package org.danila.web.controller.graphql.user

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import org.danila.generated.types.*
import org.danila.security.jwt.JwtUserPrincipal
import org.danila.services.model.user.UserService
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.util.*

@DgsComponent
class UserGraphQLEndpoint(
    private val userService: UserService
) {

    @DgsQuery
    fun getMyFavoriteTracks(
        @InputArgument size: Int,
        @InputArgument offset: Int,
        dfe: DgsDataFetchingEnvironment
    ): FavTrackConnection {
        val userId = getCurrentUserId()
        val needTotal = dfe.selectionSet.contains("totalCount")
        val result = userService.getFavoriteTracks(
            userId = userId,
            size = size,
            offset = offset,
            includeTotalCount = needTotal
        )

        return FavTrackConnection(
            items = result.tracks.map { dto ->
                FavTrack(
                    id = dto.id,
                    durationMs = dto.durationMs,
                    explicit = dto.explicit,
                    name = dto.name,
                    addedAt = dto.addedAt,
                    artists = emptyList(),
                    covers = emptyList(),
                    album = FavTrackAlbum(
                        id = dto.albumId,
                        name = dto.albumName
                    )
                )
            },
            totalCount = if (needTotal) result.totalCount else null,
            hasMore = result.hasMore
        )
    }

    @DgsQuery
    fun getMyFavoriteAlbums(
        @InputArgument size: Int,
        @InputArgument after: String?
    ): FavAlbumConnection {
        val userId = getCurrentUserId()
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

    private fun getCurrentUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated")

        val principal = authentication.principal
        if (principal !is JwtUserPrincipal) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication token")
        }

        return principal.userId
    }

}