package lk.lankaboardinghouse.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "districts")
data class District(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String = ""
)