package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.BoardingHouseRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseResponseDto
import lk.lankaboardinghouse.backend.model.BoardingHouse
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
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
        townName = b.town.name,
        districtName = b.town.district.name,
        ownerName = b.owner.fullName,
        ownerPhone = b.owner.phoneNumber,
        status = b.status
    )

    // Admin/debug: view ALL boarding houses regardless of status
    @GetMapping
    fun getAll(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findAll().map { toDto(it) }

    // Owner submits a new listing request (status = PENDING)
    @PostMapping("/request")
    fun submitRequest(@RequestBody dto: BoardingHouseRequestDto): BoardingHouseResponseDto {
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

    // Public search: only approved listings, filtered by town
    @GetMapping("/search")
    fun search(@RequestParam townId: Long): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByTownIdAndStatus(townId, BoardingHouseStatus.APPROVED)
            .map { toDto(it) }

    // Owner views their own submitted listings (any status)
    @GetMapping("/owner/{ownerId}")
    fun getByOwner(@PathVariable ownerId: Long): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByOwnerId(ownerId).map { toDto(it) }

    // Admin: view all pending requests
    @GetMapping("/pending")
    fun getPending(): List<BoardingHouseResponseDto> =
        boardingHouseRepository.findByStatus(BoardingHouseStatus.PENDING).map { toDto(it) }

    // Admin: approve a request
    @PutMapping("/{id}/approve")
    fun approve(@PathVariable id: Long): BoardingHouseResponseDto {
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        val updated = b.copy(status = BoardingHouseStatus.APPROVED)
        return toDto(boardingHouseRepository.save(updated))
    }

    // Admin: decline a request
    @PutMapping("/{id}/decline")
    fun decline(@PathVariable id: Long): BoardingHouseResponseDto {
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        val updated = b.copy(status = BoardingHouseStatus.DECLINED)
        return toDto(boardingHouseRepository.save(updated))
    }
}