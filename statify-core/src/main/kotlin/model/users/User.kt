package org.danila.model.users

import jakarta.persistence.*
import org.danila.model.spotify.SpotifyInfo
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email")
    ]
)
data class User(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "password")
    val password: String?,

    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, mappedBy = "user")
    var spotifyInfo: SpotifyInfo? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant? = null

) {

    @ElementCollection(fetch = FetchType.EAGER)
    val authorities: MutableSet<String> = mutableSetOf("ROLE_USER")

}