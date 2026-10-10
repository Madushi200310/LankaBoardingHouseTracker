package lk.lankaboardinghouse.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "boarding_houses")
data class BoardingHouse(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val rulesAndRegulations: String = "",

    @Column(nullable = false)
    val price: Double = 0.0,

    @Column(nullable = false)
    val addressLine: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_id", nullable = false)
    val town: Town = Town(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User = User(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: BoardingHouseStatus = BoardingHouseStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    val declineReason: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)