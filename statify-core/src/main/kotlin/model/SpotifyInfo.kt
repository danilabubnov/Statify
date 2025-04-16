package org.danila.model

import jakarta.persistence.*
import org.danila.converter.CryptoConverter
import org.danila.model.users.User
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "spotify_info")
data class SpotifyInfo(

    @Id
    @Column(name = "spotify_id")
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