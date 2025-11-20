package org.danila.dto

data class FavTrackDto(
    val id: String,
    val durationMs: Int,
    val explicit: Boolean,
    val name: String,
    val albumId: String,
    val albumName: String,
    val addedAt: String,
    val cursor: String
)