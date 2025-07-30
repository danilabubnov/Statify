package org.danila.dto

data class FavTrackDto(
    val id: String,
    val durationMs: Int,
    val explicit: Boolean,
    val name: String,
    val popularity: Int?,
    val trackNumber: Int,
    val albumId: String,
    val addedAt: String,
    val cursor: String
)