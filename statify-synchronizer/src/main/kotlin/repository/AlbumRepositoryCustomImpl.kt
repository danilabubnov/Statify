package org.danila.repository

import org.danila.dto.musicbrainz.releasegroup.AlbumReleaseGroupLookupResult
import org.danila.model.spotify.album.Album
import org.danila.repository.projection.album.AlbumBarcodes
import org.danila.repository.projection.album.AlbumNameLookup
import org.danila.repository.projection.album.ArtistName
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
            ),
            arg_ins AS (
                INSERT INTO album_release_groups (spotify_id) 
                SELECT spotify_id
                    FROM inserted
                ON CONFLICT (spotify_id) DO NOTHING
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

    override fun claimPendingAlbums(limit: Int): Flux<String> {
        val sql = """
            WITH cte AS (
                SELECT spotify_id
                FROM album_release_groups
                WHERE mb_release_group_status = 'PENDING'
                ORDER BY spotify_id
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            UPDATE album_release_groups arg
            SET mb_release_group_status = 'IN_PROGRESS',
                processing_started_at = NOW()
            FROM cte
            WHERE arg.spotify_id = cte.spotify_id
            RETURNING arg.spotify_id AS spotify_id;
        """.trimIndent()

        return databaseClient.sql(sql)
            .bind("limit", limit)
            .map { row, _ -> row.get("spotify_id", String::class.java)!! }
            .all()
    }

    override fun claimBarcodeNotFoundAlbums(limit: Int): Flux<String> {
        val sql = """
            WITH cte AS (
                SELECT spotify_id
                FROM album_release_groups
                WHERE mb_release_group_status = 'BARCODE_NOT_FOUND'
                ORDER BY spotify_id
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            UPDATE album_release_groups arg
            SET mb_release_group_status = 'IN_PROGRESS',
                processing_started_at = NOW()
            FROM cte
            WHERE arg.spotify_id = cte.spotify_id
            RETURNING arg.spotify_id AS spotify_id;
        """.trimIndent()

        return databaseClient.sql(sql)
            .bind("limit", limit)
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

    override fun findAlbumsForNameLookup(albumIds: Set<String>): Flux<AlbumNameLookup> {
        if (albumIds.isEmpty()) return Flux.empty()

        val placeholders = (1..albumIds.size).joinToString(", ") { "$$it" }
        val sql = """
            SELECT a.spotify_id,
                   a.name AS album_name,
                   ar.name AS artist_name
            FROM albums a
            JOIN album_artists aa ON aa.album_id = a.spotify_id
            JOIN artists ar ON ar.spotify_id = aa.artist_id
            WHERE a.spotify_id IN ($placeholders)
            ORDER BY a.spotify_id, ar.name
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albumIds.forEachIndexed { index, id ->
            spec = spec.bind(index, id)
        }

        data class Row(val spotifyId: String, val albumName: String, val artistName: String)

        return spec
            .map { row, _ ->
                Row(
                    spotifyId = row.get("spotify_id", String::class.java)!!,
                    albumName = row.get("album_name", String::class.java)!!,
                    artistName = row.get("artist_name", String::class.java)!!
                )
            }
            .all()
            .bufferUntilChanged { it.spotifyId }
            .map { rows ->
                val first = rows.first()
                val artists = rows
                    .map { it.artistName }
                    .distinct()
                    .map { ArtistName(name = it) }

                AlbumNameLookup(
                    spotifyId = first.spotifyId,
                    name = first.albumName,
                    artists = artists
                )
            }
    }

    override fun saveReleaseGroupLookupResults(albums: List<AlbumReleaseGroupLookupResult>): Mono<Void> {
        if (albums.isEmpty()) return Mono.empty()

        val placeholders = albums.indices.joinToString(", ") { idx ->
            val p1 = idx * 3 + 1
            val p2 = idx * 3 + 2
            val p3 = idx * 3 + 3
            "($$p1, $$p2, $$p3)"
        }

        val sql = """
            UPDATE album_release_groups arg
            SET mb_release_group = d.mb_release_group,
                mb_release_group_status = CASE 
                    WHEN d.mb_release_group IS NULL AND d.fetch_type = 'BY_BARCODE' THEN 'BARCODE_NOT_FOUND'
                    WHEN d.mb_release_group IS NULL AND d.fetch_type = 'BY_NAME' THEN 'NAME_NOT_FOUND'
                    ELSE 'FOUND'
                END
            FROM (VALUES $placeholders) AS d(spotify_id, mb_release_group, fetch_type)
            WHERE arg.spotify_id = d.spotify_id
        """.trimIndent()

        var spec = databaseClient.sql(sql)

        albums.forEachIndexed { index, mapping ->
            val base = index * 3

            spec = spec
                .bind(base + 0, mapping.spotifyId)
                .let { s ->
                    val rg = mapping.releaseGroupId
                    if (rg == null) s.bindNull(base + 1, String::class.java) else s.bind(base + 1, rg)
                }
                .bind(base + 2, mapping.lookupType.name)
        }

        return spec.fetch().rowsUpdated().then()
    }

}