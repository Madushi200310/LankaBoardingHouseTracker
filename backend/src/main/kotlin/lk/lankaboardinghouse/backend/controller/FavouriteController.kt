package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.BoardingHouseMapper
import lk.lankaboardinghouse.backend.dto.BoardingHouseResponseDto
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import lk.lankaboardinghouse.backend.model.Favourite
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.FavouriteRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favourites")
class FavouriteController(
    private val favouriteRepository: FavouriteRepository,
    private val boardingHouseRepository: BoardingHouseRepository,
    private val userRepository: UserRepository
) {

    private fun currentUser(authentication: Authentication): User {
        val userId = authentication.name.toLongOrNull()
            ?: throw IllegalArgumentException("Invalid session")
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
    }

    // Lists the logged-in user's favourites, newest first.
    @GetMapping
    fun list(authentication: Authentication): List<BoardingHouseResponseDto> {
        val user = currentUser(authentication)
        return favouriteRepository.findByUserIdOrderByCreatedAtDesc(user.id)
            .map { BoardingHouseMapper.toDto(it.boardingHouse) }
    }

    // Adds a favourite. Idempotent: calling twice returns 204 both times.
    @PostMapping("/{boardingHouseId}")
    fun add(
        @PathVariable boardingHouseId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val user = currentUser(authentication)

        val house = boardingHouseRepository.findById(boardingHouseId)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }

        // Only live, approved listings can be favourited.
        if (house.status != BoardingHouseStatus.APPROVED) {
            throw IllegalArgumentException("Only approved listings can be favourited")
        }

        if (!favouriteRepository.existsByUserIdAndBoardingHouseId(user.id, boardingHouseId)) {
            favouriteRepository.save(Favourite(user = user, boardingHouse = house))
        }
        return ResponseEntity.noContent().build()
    }

    // Removes a favourite. Idempotent: works even if it wasn't favourited.
    @DeleteMapping("/{boardingHouseId}")
    fun remove(
        @PathVariable boardingHouseId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val user = currentUser(authentication)
        favouriteRepository.deleteByUserIdAndBoardingHouseId(user.id, boardingHouseId)
        return ResponseEntity.noContent().build()
    }
}