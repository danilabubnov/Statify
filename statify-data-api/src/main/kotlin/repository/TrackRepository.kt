package org.danila.repository

import org.danila.model.spotify.track.Track
import org.danila.repository.projection.TrackIdProjection
import org.danila.repository.projection.ArtistImageRow
import org.danila.repository.projection.TrackImageProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TrackRepository : JpaRepository<Track, String> {

    @Query("""
        SELECT 
            t.spotifyId AS spotifyId,
            t.name      AS name
        FROM Track t
        WHERE (:releaseYear IS NULL OR t.album.releaseDate.albumReleaseYear = :releaseYear)
            AND t.popularity is not null
        ORDER BY t.popularity DESC, t.spotifyId ASC
    """)
    fun findTopTracksByPopularity(@Param("releaseYear") year: Int?, pageable: Pageable): Page<TrackIdProjection>

    @Query(
        value = """
            SELECT
                t.spotify_id  AS trackId,
                ai.image_url  AS imageUrl,
                ai.image_height AS imageHeight,
                ai.image_width  AS imageWidth,
                ai.image_order  AS imageOrder
            FROM tracks t
                JOIN album_images ai ON ai.album_id = t.album_id
            WHERE t.spotify_id IN (:trackIds)
            ORDER BY t.spotify_id, ai.image_order
        """,
        nativeQuery = true
    )
    fun findImagesForTrackIds(@Param("trackIds") trackIds: Collection<String>): List<TrackImageProjection>

}
