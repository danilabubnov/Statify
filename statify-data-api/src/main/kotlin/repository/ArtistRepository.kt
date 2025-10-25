package org.danila.repository

import org.danila.model.spotify.artist.Artist
import org.danila.repository.projection.AlbumArtistRow
import org.danila.repository.projection.ArtistImageRow
import org.danila.repository.projection.TrackArtistRow
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArtistRepository : JpaRepository<Artist, String> {

    @Query("""
        select 
          al.spotifyId as albumId,
          ar.spotifyId as artistId,
          ar.name as name,
          ar.popularity as popularity,
          ar.followersTotal as followersTotal
        from Album al
          join al.artists ar
        where al.spotifyId in :albumIds
        order by al.spotifyId, ar.name
    """)
    fun findArtistsByAlbumIds(@Param("albumIds") ids: Collection<String>): List<AlbumArtistRow>

    fun findAllByPopularityIsNotNullOrderByPopularityDesc(pageable: Pageable): Page<Artist>

    fun countByPopularityIsNotNull(): Long

    @Query(
        value = """
            SELECT ai.artist_id    AS artistId,
                   ai.image_url   AS imageUrl,
                   ai.image_height AS imageHeight,
                   ai.image_width  AS imageWidth,
                   ai.image_order  AS imageOrder
            FROM artist_images ai
            WHERE ai.artist_id IN (:artistIds)
            ORDER BY ai.artist_id, ai.image_order
        """,
        nativeQuery = true
    )
    fun findImagesForArtistIds(@Param("artistIds") artistIds: Collection<String>): List<ArtistImageRow>

    @Query("""
        select 
          tr.spotifyId as trackId,
          ar.spotifyId as artistId,
          ar.name as name,
          ar.popularity as popularity,
          ar.followersTotal as followersTotal
        from Track tr
          join tr.artists ar
        where tr.spotifyId in :trackIds
        order by tr.spotifyId, ar.name
    """)
    fun findArtistsByTrackIds(@Param("trackIds") trackIds: Collection<String>): List<TrackArtistRow>

}