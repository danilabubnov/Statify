package org.danila.repository

import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumType
import org.danila.repository.projection.AlbumIdProjection
import org.danila.repository.projection.AlbumImageProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AlbumRepository : JpaRepository<Album, String> {

    @Query(
        value = """
            WITH base AS (
                SELECT
                    alb.spotify_id,
                    alb.name,
                    alb.popularity,
                    arg.mb_release_group,
                    arg.mb_release_group_status
                FROM albums alb
                JOIN album_release_groups arg USING (spotify_id)
                WHERE alb.album_type = :#{#albumType.name()}
                    AND alb.popularity IS NOT NULL
                    AND (CAST(:from AS DATE) IS NULL OR alb.release_date_raw >= CAST(:from AS TEXT))
                    AND (CAST(:to AS DATE) IS NULL OR alb.release_date_raw <= CAST(:to AS TEXT))
            ),
            ranked AS (
                SELECT
                    b.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY
                            CASE
                                WHEN mb_release_group_status = 'FOUND' THEN mb_release_group
                                ELSE spotify_id
                            END
                        ORDER BY popularity DESC, spotify_id
                    ) rn
                FROM base b
            )
            SELECT
                r.spotify_id AS spotifyId,
                r.name as name
            FROM ranked r
            WHERE rn = 1
            ORDER BY r.popularity DESC, r.spotify_id ASC
            """,
        nativeQuery = true)
    fun findTopAlbumsByPopularity(
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        @Param("albumType") albumType: AlbumType,
        pageable: Pageable
    ): Slice<AlbumIdProjection>

    @Query(
        value = """
        WITH base AS (
            SELECT
                alb.spotify_id,
                arg.mb_release_group,
                arg.mb_release_group_status
            FROM albums alb
            JOIN album_release_groups arg USING (spotify_id)
            WHERE alb.album_type = :#{#albumType.name()}
              AND alb.popularity IS NOT NULL
              AND (CAST(:from AS DATE) IS NULL OR alb.release_date_raw >= CAST(:from AS TEXT))
              AND (CAST(:to AS DATE) IS NULL OR alb.release_date_raw <= CAST(:to AS TEXT))
        )
        SELECT
            COUNT(DISTINCT
                CASE
                    WHEN mb_release_group_status = 'FOUND' THEN mb_release_group
                    ELSE spotify_id
                END
            )::int
        FROM base
        """,
        nativeQuery = true
    )
    fun countTopAlbumsByPopularity(
        @Param("from") from: LocalDate?,
        @Param("to") to: LocalDate?,
        @Param("albumType") albumType: AlbumType
    ): Long

    @Query(
        value = """
            SELECT ai.album_id    AS albumId,
                   ai.image_url   AS imageUrl,
                   ai.image_height AS imageHeight,
                   ai.image_width  AS imageWidth,
                   ai.image_order  AS imageOrder
            FROM album_images ai
            WHERE ai.album_id IN (:albumIds)
            ORDER BY ai.album_id, ai.image_order
        """,
        nativeQuery = true
    )
    fun findImagesForAlbumIds(@Param("albumIds") albumIds: Collection<String>): List<AlbumImageProjection>

}