package org.danila.dto

data class FavAlbumDto(
    val id: String,
    val albumType: String,
    val totalTracks: Int,
    val name: String,
    val label: String?,
    val popularity: Int?,
    val releaseDate: String,
    val addedAt: String,
    val cursor: String
)