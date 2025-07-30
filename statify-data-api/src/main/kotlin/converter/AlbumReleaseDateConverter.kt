package org.danila.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.danila.model.spotify.album.AlbumReleaseDatePrecision

@Converter(autoApply = true)
class AlbumReleaseDateConverter : AttributeConverter<Pair<String, AlbumReleaseDatePrecision>, String> {

    override fun convertToDatabaseColumn(attribute: Pair<String, AlbumReleaseDatePrecision>): String {
        return attribute.first
    }

    override fun convertToEntityAttribute(dbData: String): Pair<String, AlbumReleaseDatePrecision> {
        return parseReleaseDate(dbData)
    }

    private fun parseReleaseDate(dateStr: String): Pair<String, AlbumReleaseDatePrecision> {
        return when (dateStr.split("-").size) {
            1 -> dateStr to AlbumReleaseDatePrecision.YEAR
            2 -> dateStr to AlbumReleaseDatePrecision.MONTH
            3 -> dateStr to AlbumReleaseDatePrecision.DAY
            else -> throw IllegalArgumentException("Invalid date format: $dateStr")
        }
    }

}