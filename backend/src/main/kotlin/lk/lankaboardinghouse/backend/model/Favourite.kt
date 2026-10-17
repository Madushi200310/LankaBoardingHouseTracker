package lk.lankaboardinghouse.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "favourites",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_favourite_user_house",
            columnNames = ["user_id", "boarding_house_id"]
        )
    ]
)
data class Favourite(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_house_id", nullable = false)
    val boardingHouse: BoardingHouse = BoardingHouse(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)