package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.BoardingHouseRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseResponseDto
import lk.lankaboardinghouse.backend.dto.DeclineRequestDto
import lk.lankaboardinghouse.backend.model.BoardingHouse
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/boarding-houses")
class BoardingHouseController(
    private val boardingHouseRepository: BoardingHouseRepository,
    private val townRepository: TownRepository,
    private val userRepository: UserRepository
) {

    private fun toDto(b: BoardingHouse) = BoardingHouseResponseDto(
        id = b.id,
        title = b.title,
        description = b.description,
        rulesAndRegulations = b.rulesAndRegulations,
        price = b.price,
        addressLine = b.addressLine,
        townId = b.town.id,
        townName = b.town.name,
        districtId = b.town.district.id,
        districtName = b.town.district.name,
        ownerName = b.owner.fullName,
        ownerPhone = b.owner.phoneNumber,
        status = b.status,
        declineReason = b.declineReason
    )

    // The token's subject is the user id; make sure it matches the id in the request
    private fun requireSelf(authentication: Authentication, ownerId: Long) {
        if (authentication.name != ownerId.toString()) {
            throw AccessDeniedException("Not allowed to act for another user")
        }
    }

    // Admin/debug: view ALL boarding houses regardless of status
    @GetMapping
    fun getAll(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findAll().map { toDto(it) }

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

        return toDto(boardingHouseRepository.save(boardingHouse))
    }

    // Owner edits a DECLINED listing and resubmits it (status goes back to PENDING)
    @PutMapping("/{id}")
    fun resubmit(
        @PathVariable id: Long,
        @RequestBody dto: BoardingHouseRequestDto,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        val existing = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }

        if (existing.owner.id.toString() != authentication.name) {
            throw AccessDeniedException("This is not your listing")
        }
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

        return toDto(boardingHouseRepository.save(updated))
    }

    // Public search: only approved listings, filtered by town
    @GetMapping("/search")
    fun search(@RequestParam townId: Long): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByTownIdAndStatus(townId, BoardingHouseStatus.APPROVED)
            .map { toDto(it) }

    // Owner views their own submitted listings (any status)
    @GetMapping("/owner/{ownerId}")
    fun getByOwner(
        @PathVariable ownerId: Long,
        authentication: Authentication
    ): List<BoardingHouseResponseDto> {
        requireSelf(authentication, ownerId)
        return boardingHouseRepository.findByOwnerId(ownerId).map { toDto(it) }
    }

    // Admin: view all pending requests
    @GetMapping("/pending")
    fun getPending(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByStatus(BoardingHouseStatus.PENDING).map { toDto(it) }

    // Admin: approve a request
    @PutMapping("/{id}/approve")
    fun approve(@PathVariable id: Long): BoardingHouseResponseDto {
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        val updated = b.copy(status = BoardingHouseStatus.APPROVED, declineReason = null)
        return toDto(boardingHouseRepository.save(updated))
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
        return toDto(boardingHouseRepository.save(updated))
    }
}