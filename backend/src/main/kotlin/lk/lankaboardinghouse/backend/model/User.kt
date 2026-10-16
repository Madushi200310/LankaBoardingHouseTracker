package lk.lankaboardinghouse.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val fullName: String = "",

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(nullable = false)
    val password: String = "",

    @Column(nullable = false)
    val phoneNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER,

    // Bumped whenever the user's password changes OR their account is
    // disabled/enabled. Any token carrying an older version is rejected
    // by JwtAuthFilter.
    @Column(nullable = false, columnDefinition = "bigint default 0")
    val tokenVersion: Long = 0,

    // When true, login is blocked and existing tokens stop working.
    @Column(nullable = false, columnDefinition = "boolean default false")
    val disabled: Boolean = false,

    @Column(nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)