package lk.lankaboardinghouse.backend.repository

import lk.lankaboardinghouse.backend.model.Favourite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FavouriteRepository : JpaRepository<Favourite, Long> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Favourite>

    fun existsByUserIdAndBoardingHouseId(userId: Long, boardingHouseId: Long): Boolean

    // Used by BoardingHouseController.delete to cascade-delete favourites
    // before the listing itself is removed.
    fun deleteByBoardingHouseId(boardingHouseId: Long)

    fun deleteByUserIdAndBoardingHouseId(userId: Long, boardingHouseId: Long)
}