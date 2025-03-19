package org.danila.model.spotify

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.danila.converter.CryptoConverter
import org.danila.model.users.User
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "spotify_info",
    indexes = [
        Index(name = "idx_user_spotify_id", columnList = "spotify_id")
    ]
)
data class SpotifyInfo(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "spotify_id", unique = true)
    var spotifyId: String,

    @Column(name = "email")
    var email: String,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(name = "access_token", length = 1000)
    @Convert(converter = CryptoConverter::class)
    var accessToken: String?,

    @Column(name = "refresh_token", length = 1000)
    @Convert(converter = CryptoConverter::class)
    var refreshToken: String?,

    @Column(name = "expires_at")
    var expiresAt: Instant?,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

)