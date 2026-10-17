package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.AvailabilityRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseMapper
import lk.lankaboardinghouse.backend.dto.BoardingHouseRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseResponseDto
import lk.lankaboardinghouse.backend.dto.DeclineRequestDto
import lk.lankaboardinghouse.backend.model.BoardingHouse
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.FavouriteRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boarding-houses")
class BoardingHouseController(
    private val boardingHouseRepository: BoardingHouseRepository,
    private val townRepository: TownRepository,
    private val userRepository: UserRepository,
    private val favouriteRepository: FavouriteRepository
) {

    // The token's subject is the user id; make sure it matches the id in the request
    private fun requireSelf(authentication: Authentication, ownerId: Long) {
        if (authentication.name != ownerId.toString()) {
            throw AccessDeniedException("Not allowed to act for another user")
        }
    }

    // Loads a listing and makes sure the logged-in owner really owns it
    private fun findOwnedListing(id: Long, authentication: Authentication): BoardingHouse {
        val existing = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        if (existing.owner.id.toString() != authentication.name) {
            throw AccessDeniedException("This is not your listing")
        }
        return existing
    }

    // Admin/debug: view ALL boarding houses regardless of status
    @GetMapping
    fun getAll(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findAll().map { BoardingHouseMapper.toDto(it) }

    // Owner submits a new listing request (status = PENDING)
    @PostMapping("/request")
    fun submitRequest(
        @RequestBody dto: BoardingHouseRequestDto,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        requireSelf(authentication, dto.ownerId)

        val town = townRepository.findById(dto.townId)
            .orElseThrow { IllegalArgumentException("Town not found") }
        val owner = userRepository.findById(dto.ownerId)
            .orElseThrow { IllegalArgumentException("Owner not found") }

        val boardingHouse = BoardingHouse(
            title = dto.title,
            description = dto.description,
            rulesAndRegulations = dto.rulesAndRegulations,
            price = dto.price,
            addressLine = dto.addressLine,
            town = town,
            owner = owner,
            status = BoardingHouseStatus.PENDING
        )

        return BoardingHouseMapper.toDto(boardingHouseRepository.save(boardingHouse))
    }

    // Owner edits a DECLINED listing and resubmits it (status goes back to PENDING)
    @PutMapping("/{id}")
    fun resubmit(
        @PathVariable id: Long,
        @RequestBody dto: BoardingHouseRequestDto,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        val existing = findOwnedListing(id, authentication)

        if (existing.status != BoardingHouseStatus.DECLINED) {
            throw IllegalArgumentException("Only declined listings can be edited and resubmitted")
        }

        val town = townRepository.findById(dto.townId)
            .orElseThrow { IllegalArgumentException("Town not found") }

        val updated = existing.copy(
            title = dto.title,
            description = dto.description,
            rulesAndRegulations = dto.rulesAndRegulations,
            price = dto.price,
            addressLine = dto.addressLine,
            town = town,
            status = BoardingHouseStatus.PENDING,
            declineReason = null
        )

        return BoardingHouseMapper.toDto(boardingHouseRepository.save(updated))
    }

    // Owner marks an approved listing as full (hidden from search) or available again
    @PutMapping("/{id}/availability")
    fun setAvailability(
        @PathVariable id: Long,
        @RequestBody dto: AvailabilityRequestDto,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        val existing = findOwnedListing(id, authentication)

        if (existing.status != BoardingHouseStatus.APPROVED) {
            throw IllegalArgumentException("Only approved listings can be marked as full or available")
        }

        return BoardingHouseMapper.toDto(
            boardingHouseRepository.save(existing.copy(available = dto.available))
        )
    }

    // Owner permanently deletes one of their own listings.
    // Favourites that reference it are removed first, so the DB never sees
    // a foreign-key violation.
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val existing = findOwnedListing(id, authentication)
        favouriteRepository.deleteByBoardingHouseId(id)
        boardingHouseRepository.delete(existing)
        return ResponseEntity.noContent().build()
    }

    // Search approved, available listings in a town, with optional price range and sorting
    // sort: "newest" (default), "price_asc", "price_desc"
    @GetMapping("/search")
    fun search(
        @RequestParam townId: Long,
        @RequestParam(required = false) minPrice: Double?,
        @RequestParam(required = false) maxPrice: Double?,
        @RequestParam(required = false, defaultValue = "newest") sort: String
    ): List<BoardingHouseResponseDto> {
        val min = minPrice ?: 0.0
        val max = maxPrice ?: 1.0e12

        if (min > max) {
            throw IllegalArgumentException("minPrice cannot be greater than maxPrice")
        }

        val results = boardingHouseRepository.findByTownIdAndStatusAndAvailableTrueAndPriceBetween(
            townId, BoardingHouseStatus.APPROVED, min, max
        )

        val sorted = when (sort) {
            "price_asc" -> results.sortedBy { it.price }
            "price_desc" -> results.sortedByDescending { it.price }
            else -> results.sortedByDescending { it.createdAt }
        }

        return sorted.map { BoardingHouseMapper.toDto(it) }
    }

    // Owner views their own submitted listings (any status)
    @GetMapping("/owner/{ownerId}")
    fun getByOwner(
        @PathVariable ownerId: Long,
        authentication: Authentication
    ): List<BoardingHouseResponseDto> {
        requireSelf(authentication, ownerId)
        return boardingHouseRepository.findByOwnerId(ownerId).map { BoardingHouseMapper.toDto(it) }
    }

    // Admin: view all pending requests
    @GetMapping("/pending")
    fun getPending(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByStatus(BoardingHouseStatus.PENDING).map { BoardingHouseMapper.toDto(it) }

    // Admin: approve a request
    @PutMapping("/{id}/approve")
    fun approve(@PathVariable id: Long): BoardingHouseResponseDto {
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        val updated = b.copy(status = BoardingHouseStatus.APPROVED, declineReason = null)
        return BoardingHouseMapper.toDto(boardingHouseRepository.save(updated))
    }

    // Admin: decline a request, with a reason the owner will see
    @PutMapping("/{id}/decline")
    fun decline(
        @PathVariable id: Long,
        @RequestBody dto: DeclineRequestDto
    ): BoardingHouseResponseDto {
        if (dto.reason.isBlank()) {
            throw IllegalArgumentException("A decline reason is required")
        }
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        val updated = b.copy(status = BoardingHouseStatus.DECLINED, declineReason = dto.reason.trim())
        return BoardingHouseMapper.toDto(boardingHouseRepository.save(updated))
    }
}