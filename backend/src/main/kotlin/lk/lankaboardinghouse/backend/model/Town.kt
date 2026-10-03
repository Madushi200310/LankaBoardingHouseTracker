package lk.lankaboardinghouse.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "towns")
data class Town(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    val district: District = District()
)