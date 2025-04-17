package config

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.danila.dto.album.AlbumDTO
import org.danila.dto.album.AlbumSimpleDTO
import org.danila.dto.artist.ArtistDTO
import org.danila.dto.artist.ArtistSimpleDTO
import org.danila.dto.common.FollowersDTO
import org.danila.dto.common.ImageDTO
import org.danila.dto.track.TrackDTO
import org.danila.dto.track.TrackItemDTO
import org.danila.dto.track.TracksDTO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [UtilsJacksonConfiguration::class])
class SpotifyObjectMapperTest @Autowired constructor(
    private val objectMapper: ObjectMapper
) {

    @Test
    fun `should configure DeserializationFeature FAIL_ON_UNKNOWN_PROPERTIES to false`() {
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse()
    }

    @Test
    fun `should convert null collection fields to empty collections`() {
        val json = """
            {
              "album_type": "album",
              "total_tracks": 10,
              "id": "test",
              "images": [],
              "name": "Test Album",
              "release_date": "2025-01-01",
              "release_date_precision": "day",
              "artists": null,
              "tracks": {"limit":0,"next":null,"offset":0,"previous":null,"total":0,"items":[]},
              "label": "Test Label",
              "popularity": 5
            }
        """.trimIndent()

        val album: AlbumDTO = objectMapper.readValue(json)

        assertThat(album.artists).isEqualTo(emptyList<Any>())
    }

    @Test
    fun `should ignore unknown properties during deserialization`() {
        val json = """
            {
              "album_type": "album",
              "total_tracks": 10,
              "id": "test",
              "images": [],
              "name": "Test Album",
              "release_date": "2025-01-01",
              "release_date_precision": "day",
              "artists": [],
              "tracks": {"limit":0,"next":null,"offset":0,"previous":null,"total":0,"items":[]},
              "label": "Test Label",
              "popularity": 5,
              "unknown": "field"
            }
        """.trimIndent()

        val album: AlbumDTO = objectMapper.readValue(json)

        assertThat(album.name).isEqualTo("Test Album")
    }

    @Test
    fun `should deserialize AlbumDTO json correctly`() {
        val json = """
            {
              "album_type": "album",
              "total_tracks": 18,
              "id": "4aawyAB9vmqN3uQ7FjRGTy",
              "images": [
                {
                  "url": "https://i.scdn.co/image/ab67616d0000b2732c5b24ecfa39523a75c993c4",
                  "height": 640,
                  "width": 640
                }
              ],
              "name": "Global Warming",
              "release_date": "2012-11-16",
              "release_date_precision": "day",
              "artists": [
                {
                  "id": "0TnOYISbd1XYRBk9myaseg",
                  "name": "Pitbull"
                }
              ],
              "tracks": {
                "limit": 50,
                "next": null,
                "offset": 0,
                "previous": null,
                "total": 18,
                "items": [
                  {
                    "artists": [
                      {
                        "id": "0TnOYISbd1XYRBk9myaseg",
                        "name": "Pitbull"
                      }
                    ],
                    "duration_ms": 85400,
                    "explicit": true,
                    "id": "6OmhkSOpvYBokMKQxpIGx2",
                    "name": "Global Warming (feat. Sensato)",
                    "track_number": 1
                  }
                ]
              },
              "label": "Mr.305/Polo Grounds Music",
              "popularity": 54
            }
        """.trimIndent()

        val album: AlbumDTO = objectMapper.readValue(json)

        assertThat(album).isEqualTo(
            AlbumDTO(
                albumType = "album",
                totalTracks = 18,
                id = "4aawyAB9vmqN3uQ7FjRGTy",
                name = "Global Warming",
                releaseDate = "2012-11-16",
                releaseDatePrecision = "day",
                images = listOf(
                    ImageDTO(url = "https://i.scdn.co/image/ab67616d0000b2732c5b24ecfa39523a75c993c4", height = 640, width = 640)
                ),
                artists = listOf(ArtistSimpleDTO(id = "0TnOYISbd1XYRBk9myaseg", name = "Pitbull")),
                tracks = TracksDTO(
                    items = listOf(
                        TrackItemDTO(
                            artists = listOf(ArtistSimpleDTO(id = "0TnOYISbd1XYRBk9myaseg", name = "Pitbull")),
                            durationMs = 85400,
                            explicit = true,
                            id = "6OmhkSOpvYBokMKQxpIGx2",
                            name = "Global Warming (feat. Sensato)",
                            trackNumber = 1
                        )
                    )
                ),
                label = "Mr.305/Polo Grounds Music",
                popularity = 54
            )
        )
    }

    @Test
    fun `should deserialize ArtistDTO json correctly`() {
        val json = """
            {
              "followers": { "total": 11446323 },
              "genres": [],
              "id": "0TnOYISbd1XYRBk9myaseg",
              "images": [
                {
                  "url": "https://i.scdn.co/image/ab6761610000e5eb4051627b19277613e0e62a34",
                  "height": 640,
                  "width": 640
                }
              ],
              "name": "Pitbull",
              "popularity": 87
            }
        """.trimIndent()

        val artist: ArtistDTO = objectMapper.readValue(json)

        assertThat(artist).isEqualTo(
            ArtistDTO(
                followers = FollowersDTO(total = 11446323),
                genres = emptyList(),
                id = "0TnOYISbd1XYRBk9myaseg",
                images = listOf(
                    ImageDTO(url = "https://i.scdn.co/image/ab6761610000e5eb4051627b19277613e0e62a34", height = 640, width = 640)
                ),
                name = "Pitbull",
                popularity = 87
            )
        )
    }

    @Test
    fun `should deserialize TrackDTO json correctly`() {
        val json = """
            {
              "album": {
                "album_type": "single",
                "total_tracks": 1,
                "id": "0tGPJ0bkWOUmH7MEOR77qc",
                "images": [
                  {
                    "url": "https://i.scdn.co/image/ab67616d0000b2737359994525d219f64872d3b1",
                    "height": 640,
                    "width": 640
                  }
                ],
                "name": "Cut To The Feeling",
                "release_date": "2017-05-26",
                "release_date_precision": "day",
                "artists": [
                  {
                    "id": "6sFIWsNpZYqfjUpaCgueju",
                    "name": "Carly Rae Jepsen"
                  }
                ]
              },
              "artists": [
                {
                  "id": "6sFIWsNpZYqfjUpaCgueju",
                  "name": "Carly Rae Jepsen"
                }
              ],
              "duration_ms": 207959,
              "explicit": false,
              "id": "11dFghVXANMlKmJXsNCbNl",
              "name": "Cut To The Feeling",
              "popularity": 0,
              "track_number": 1
            }
        """.trimIndent()

        val track: TrackDTO = objectMapper.readValue(json)

        assertThat(track).isEqualTo(
            TrackDTO(
                album = AlbumSimpleDTO(
                    albumType = "single",
                    totalTracks = 1,
                    id = "0tGPJ0bkWOUmH7MEOR77qc",
                    images = listOf(
                        ImageDTO(url = "https://i.scdn.co/image/ab67616d0000b2737359994525d219f64872d3b1", height = 640, width = 640)
                    ),
                    name = "Cut To The Feeling",
                    releaseDate = "2017-05-26",
                    releaseDatePrecision = "day",
                    artists = listOf(ArtistSimpleDTO(id = "6sFIWsNpZYqfjUpaCgueju", name = "Carly Rae Jepsen"))
                ),
                artists = listOf(ArtistSimpleDTO(id = "6sFIWsNpZYqfjUpaCgueju", name = "Carly Rae Jepsen")),
                durationMs = 207959,
                explicit = false,
                id = "11dFghVXANMlKmJXsNCbNl",
                name = "Cut To The Feeling",
                popularity = 0,
                trackNumber = 1
            )
        )
    }

}