package org.danila.model.spotify.album

import jakarta.persistence.*
import kotlin.jvm.Transient

@Embeddable
data class AlbumReleaseDate(

    @Column(name = "release_date_raw", length = 10)
    val albumReleaseDateRaw: String,

    @Column(name = "release_date_precision", length = 10)
    @Enumerated(EnumType.STRING)
    val albumReleaseDatePrecision: AlbumReleaseDatePrecision,

    @Column(name = "release_year")
    val albumReleaseYear: Int,

    @Column(name = "release_month")
    val albumReleaseMonth: Int?,

    @Column(name = "release_day")
    val albumReleaseDay: Int?,

    @Transient
    private var parsedAlbumReleaseDate: Pair<String, AlbumReleaseDatePrecision>? = null

) {

    constructor(releaseDate: String) : this(
        albumReleaseDateRaw = releaseDate,
        albumReleaseDatePrecision = parsePrecision(releaseDate),
        albumReleaseYear = parseYear(releaseDate),
        albumReleaseMonth = parseMonth(releaseDate),
        albumReleaseDay = parseDay(releaseDate)
    )

    companion object {
        private fun parseYear(date: String) = date.substring(0, 4).toInt()
        private fun parseMonth(date: String) = date.split("-").getOrNull(1)?.toInt()
        private fun parseDay(date: String) = date.split("-").getOrNull(2)?.toInt()
        private fun parsePrecision(releaseDate: String): AlbumReleaseDatePrecision {
            return when {
                releaseDate.contains("-") -> {
                    when (releaseDate.count { it == '-' }) {
                        1 -> AlbumReleaseDatePrecision.MONTH
                        2 -> AlbumReleaseDatePrecision.DAY
                        else -> throw IllegalArgumentException("Некорректный формат даты")
                    }
                }

                else -> AlbumReleaseDatePrecision.YEAR
            }
        }
    }

    @PrePersist
    @PreUpdate
    fun validateDates() {
        when (albumReleaseDatePrecision) {
            AlbumReleaseDatePrecision.YEAR -> require(albumReleaseDateRaw.matches(Regex("\\d{4}")))
            AlbumReleaseDatePrecision.MONTH -> require(albumReleaseDateRaw.matches(Regex("\\d{4}-\\d{2}")))
            AlbumReleaseDatePrecision.DAY -> require(albumReleaseDateRaw.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }
    }

}