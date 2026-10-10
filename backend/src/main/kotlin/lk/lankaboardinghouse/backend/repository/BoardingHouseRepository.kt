package lk.lankaboardinghouse.backend.repository

import lk.lankaboardinghouse.backend.model.BoardingHouse
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardingHouseRepository : JpaRepository<BoardingHouse, Long> {
    fun findByStatus(status: BoardingHouseStatus): List<BoardingHouse>
    fun findByTownIdAndStatus(townId: Long, status: BoardingHouseStatus): List<BoardingHouse>

    // Search: approved, still available (not marked full), within the price range
    fun findByTownIdAndStatusAndAvailableTrueAndPriceBetween(
        townId: Long,
        status: BoardingHouseStatus,
        minPrice: Double,
        maxPrice: Double
    ): List<BoardingHouse>

    fun findByOwnerId(ownerId: Long): List<BoardingHouse>
}