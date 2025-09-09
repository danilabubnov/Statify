package org.danila.repository

import org.danila.dto.musicbrainz.release.AlbumReleaseGroupMapping
import org.danila.model.spotify.album.Album
import org.danila.model.spotify.album.AlbumBarcodes
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class AlbumRepositoryCustomImpl(
    private val databaseClient: DatabaseClient
) : AlbumRepositoryCustom {

    override fun upsertAndReturnSimpleAlbums(albums: Collection<Album>): Flux<String> {
        if (albums.isEmpty()) return Flux.empty()

        val placeholders = albums.indices.joinToString(", ") { index ->
            "(" + ((index * 13 + 1)..(index * 13 + 13)).joinToString(", ") { "$$it" } + ")"
        }

        val sql = """
            WITH inserted AS (
                INSERT INTO albums (
                    spotify_id,
                    album_type,
                    label,
                    name,
                    popularity,
                    release_date_precision,
                    release_date_raw,
                    release_day,
                    release_month,
                    release_year,
                    total_tracks,
                    upc,
                    ean
                )
            VALUES $placeholders
            ON CONFLICT (spotify_id) 
            DO UPDATE
                SET
                    label         = EXCLUDED.label,
                    popularity    = EXCLUDED.popularity,
                    release_day   = EXCLUDED.release_day,
                    release_month = EXCLUDED.release_month,
                    upc           = EXCLUDED.upc,
                    ean           = EXCLUDED.ean
                WHERE 
                    albums.label         IS DISTINCT FROM EXCLUDED.label OR
                    albums.popularity    IS DISTINCT FROM EXCLUDED.popularity OR 
                    albums.release_day   IS DISTINCT FROM EXCLUDED.release_day OR
                    albums.release_month IS DISTINCT FROM EXCLUDED.release_month OR 
                    albums.upc           IS DISTINCT FROM EXCLUDED.upc OR
                    albums.ean           IS DISTINCT FROM EXCLUDED.ean
                RETURNING
                    spotify_id,
                    (label IS NULL OR popularity IS NULL) AS is_simple
            )
            SELECT spotify_id
                FROM inserted
            WHERE is_simple
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albums.forEachIndexed { index, album ->
            val label = album.label
            val popularity = album.popularity
            val releaseDay = album.releaseDay
            val releaseMonth = album.releaseMonth
            val upc = album.upc
            val ean = album.ean
            val base = index * 13

            spec = spec
                .bind(base + 0, album.spotifyId)
                .bind(base + 1, album.albumType)
                .let { s ->
                    if (label == null) s.bindNull(base + 2, String::class.java)
                    else s.bind(base + 2, label)
                }
                .bind(base + 3, album.name)
                .let { s ->
                    if (popularity == null) s.bindNull(base + 4, Int::class.java)
                    else s.bind(base + 4, popularity)
                }
                .bind(base + 5, album.releaseDatePrecision)
                .bind(base + 6, album.releaseDateRaw)
                .let { s ->
                    if (releaseDay == null) s.bindNull(base + 7, Int::class.java)
                    else s.bind(base + 7, releaseDay)
                }
                .let { s ->
                    if (releaseMonth == null) s.bindNull(base + 8, Int::class.java)
                    else s.bind(base + 8, releaseMonth)
                }
                .bind(base + 9, album.releaseYear)
                .bind(base + 10, album.totalTracks)
                .let { s ->
                    if (upc == null) s.bindNull(base + 11, String::class.java)
                    else s.bind(base + 11, upc)
                }
                .let { s ->
                    if (ean == null) s.bindNull(base + 12, String::class.java)
                    else s.bind(base + 12, ean)
                }
        }

        return spec
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

    override fun claimPendingBatch(limit: Int): Flux<String> {
        val sql = """
            WITH cte AS (
                SELECT spotify_id
                FROM albums
                WHERE mb_release_group_status = 'PENDING'
                ORDER BY spotify_id
                FOR UPDATE SKIP LOCKED
                LIMIT $limit
            )
            UPDATE albums a
            SET mb_release_group_status = 'IN_PROGRESS',
                processing_started_at = NOW()
            FROM cte
            WHERE a.spotify_id = cte.spotify_id
            RETURNING a.spotify_id;
        """.trimIndent()

        val spec = databaseClient.sql(sql)

        return spec
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

    override fun findAlbumsWithBarcode(albumIds: Set<String>): Flux<AlbumBarcodes> {
        if (albumIds.isEmpty()) return Flux.empty()

        val placeholders = (1..albumIds.size).joinToString(", ") { "$$it" }
        val sql = """
            SELECT spotify_id, ean, upc
            FROM albums
            WHERE spotify_id IN ($placeholders)
              AND (ean IS NOT NULL OR upc IS NOT NULL)
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albumIds.forEachIndexed { index, id ->
            spec = spec.bind(index, id)
        }

        return spec
            .map { row, _ ->
                AlbumBarcodes(
                    spotifyId = row.get("spotify_id", String::class.java)!!,
                    ean = row.get("ean", String::class.java),
                    upc = row.get("upc", String::class.java)
                )
            }
            .all()
    }

    override fun persistReleaseGroupsForAlbums(albums: List<AlbumReleaseGroupMapping>): Mono<Void> {
        if (albums.isEmpty()) return Mono.empty()

        val placeholders = albums.indices.joinToString(", ") { idx ->
            val p1 = idx * 2 + 1
            val p2 = idx * 2 + 2
            "($$p1, $$p2)"
        }

        val sql = """
            UPDATE albums a
            SET mb_release_group = d.mb_release_group,
                mb_release_group_status = CASE 
                                        WHEN d.mb_release_group IS NULL THEN 'NOT_FOUND' 
                                        ELSE 'FOUND' 
                                    END
            FROM (VALUES $placeholders) AS d(spotify_id, mb_release_group)
            WHERE a.spotify_id = d.spotify_id
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albums.forEachIndexed { index, mapping ->
            val base = index * 2

            spec = spec
                .bind(base + 0, mapping.spotifyId)
                .let { s ->
                    val rg = mapping.releaseGroupId
                    if (rg == null) s.bindNull(base + 1, String::class.java) else s.bind(base + 1, rg)
                }
        }

        return spec.fetch().rowsUpdated().then()
    }

}