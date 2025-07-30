package org.danila.services.model.user

import org.danila.dto.FavAlbumDto
import org.danila.dto.FavTrackDto
import org.danila.dto.PageInfoDto
import org.danila.model.spotify.album.UserAlbumFavoriteId
import org.danila.model.spotify.album.UserFavoriteAlbum
import org.danila.model.spotify.track.UserTrackFavoriteId
import org.danila.repository.UserFavoriteAlbumRepository
import org.danila.repository.UserFavoriteTrackRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService @Autowired constructor(
    private val userFavoriteTrackRepository: UserFavoriteTrackRepository,
    private val userFavoriteAlbumRepository: UserFavoriteAlbumRepository,
) {

    @Transactional(readOnly = true)
    fun getFavoriteTracks(
        userId: UUID,
        size: Int,
        after: String?
    ): Pair<List<FavTrackDto>, PageInfoDto> {
        val pageable = PageRequest.of(
            0,
            size,
            Sort.by(Sort.Order.desc("addedAt"), Sort.Order.desc("id.trackId"))
        )

        val slice = if (after == null)
            userFavoriteTrackRepository.findByIdUserId(userId = userId, pageable = pageable)
        else {
            val last = userFavoriteTrackRepository
                .findById(UserTrackFavoriteId(userId = userId, trackId = after))
                .orElseThrow { IllegalArgumentException("Favorite track not found for cursor=$after") }

            userFavoriteTrackRepository.findByIdUserIdAndAddedAtLessThanOrAddedAtEqualsAndIdTrackIdLessThan(
                userId = userId,
                addedAt = last.addedAt,
                sameAddedAt = last.addedAt,
                trackId = last.id.trackId,
                pageable = pageable
            )
        }

        val dtos = slice.content.map { entity ->
            FavTrackDto(
                id          = entity.id.trackId,
                durationMs  = entity.track.durationMs,
                explicit    = entity.track.explicit,
                name        = entity.track.name,
                popularity  = entity.track.popularity,
                trackNumber = entity.track.trackNumber,
                albumId     = entity.track.album.spotifyId,
                addedAt     = entity.addedAt.toString(),
                cursor      = entity.id.trackId
            )
        }

        val pageInfo = PageInfoDto(
            hasNextPage     = slice.hasNext(),
            hasPreviousPage = after != null,
            startCursor     = dtos.firstOrNull()?.cursor,
            endCursor       = dtos.lastOrNull()?.cursor
        )

        return dtos to pageInfo
    }

    @Transactional(readOnly = true)
    fun getFavoriteAlbums(
        userId: UUID,
        size: Int,
        after: String?
    ): Pair<List<FavAlbumDto>, PageInfoDto> {
        val pageable = PageRequest.of(
            0,
            size,
            Sort.by(
                Sort.Order.desc("addedAt"),
                Sort.Order.desc("id.albumId")
            )
        )

        val slice: Slice<UserFavoriteAlbum> = if (after == null) {
            userFavoriteAlbumRepository.findByIdUserId(userId = userId, pageable = pageable)
        } else {
            val last = userFavoriteAlbumRepository
                .findById(UserAlbumFavoriteId(userId = userId, albumId = after))
                .orElseThrow { IllegalArgumentException("Favorite album not found for cursor=$after") }

            userFavoriteAlbumRepository.findByIdUserIdAndAddedAtLessThanOrAddedAtEqualsAndIdAlbumIdLessThan(
                userId      = userId,
                addedAt     = last.addedAt,
                sameAddedAt = last.addedAt,
                albumId     = last.id.albumId,
                pageable    = pageable
            )
        }

        val dtos = slice.content.map { entity ->
            val album = entity.album
            FavAlbumDto(
                id          = album.spotifyId,
                albumType   = album.albumType.name,
                totalTracks = album.totalTracks,
                name        = album.name,
                label       = album.label,
                popularity  = album.popularity,
                releaseDate = album.releaseDate.toString(),
                addedAt     = entity.addedAt.toString(),
                cursor      = entity.id.albumId
            )
        }

        val pageInfo = PageInfoDto(
            hasNextPage     = slice.hasNext(),
            hasPreviousPage = after != null,
            startCursor     = dtos.firstOrNull()?.cursor,
            endCursor       = dtos.lastOrNull()?.cursor
        )

        return dtos to pageInfo
    }

}