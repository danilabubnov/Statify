package org.danila.repository

import org.danila.model.spotify.track.Track
import org.danila.repository.projection.TrackIdProjection
import org.danila.repository.projection.TrackImageProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TrackRepository : JpaRepository<Track, String> {

    @Query(
        value = """
            SELECT
                t.spotify_id AS spotifyId,
                t.name       AS name
            FROM tracks t
            JOIN albums a ON a.spotify_id = t.album_id
            WHERE (CAST(:from AS DATE) IS NULL OR a.release_date_raw >= CAST(:from AS TEXT))
                AND (CAST(:to AS DATE) IS NULL OR a.release_date_raw <= CAST(:to AS TEXT))
                AND t.popularity IS NOT NULL
            ORDER BY t.popularity DESC, t.spotify_id ASC
        """,
        nativeQuery = true
    )
    fun findTopTracksByPopularity(
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        pageable: Pageable
    ): Slice<TrackIdProjection>

    @Query(
        value = """
            SELECT
                COUNT(t.spotify_id)
            FROM tracks t
            JOIN albums a ON a.spotify_id = t.album_id
            WHERE (CAST(:from AS DATE) IS NULL OR a.release_date_raw >= CAST(:from AS TEXT))
                AND (CAST(:to AS DATE) IS NULL OR a.release_date_raw <= CAST(:to AS TEXT))
                AND t.popularity IS NOT NULL
        """,
        nativeQuery = true
    )
    fun countTopTracksByPopularity(
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?
    ): Long

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
