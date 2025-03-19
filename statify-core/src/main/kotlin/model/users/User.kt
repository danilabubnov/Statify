package org.danila.model.users

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.danila.model.spotify.SpotifyInfo
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_username", columnList = "username")
    ]
)
data class User(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "username", nullable = false, unique = true)
    var username: String,

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