package org.danila.repository.projection.album

data class AlbumBarcodes(
    val spotifyId: String,
    val ean: String?,
    val upc: String?
)