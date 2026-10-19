package lk.lankaboardinghouse.backend.controller

import jakarta.validation.Valid
import lk.lankaboardinghouse.backend.dto.AvailabilityRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseRequestDto
import lk.lankaboardinghouse.backend.dto.BoardingHouseResponseDto
import lk.lankaboardinghouse.backend.dto.DeclineRequestDto
import lk.lankaboardinghouse.backend.model.BoardingHouse
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestController
@RequestMapping("/api/boarding-houses")
class BoardingHouseController(
    private val boardingHouseRepository: BoardingHouseRepository,
    private val townRepository: TownRepository,
    private val userRepository: UserRepository
) {

    companion object {
        private const val MAX_PHOTOS = 5
        private const val MAX_PHOTO_BYTES = 5L * 1024 * 1024 // 5 MB
        private val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        private val UPLOAD_DIR: java.nio.file.Path = Paths.get("uploads").toAbsolutePath().normalize()
    }

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
        declineReason = b.declineReason,
        available = b.available,
        photoUrls = b.photoUrls.toList()
    )

    private fun requireSelf(authentication: Authentication, ownerId: Long) {
        if (authentication.name != ownerId.toString()) {
            throw AccessDeniedException("Not allowed to act for another user")
        }
    }

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
        boardingHouseRepository.findAll().map { toDto(it) }

    // Single listing by id (any logged-in user)
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): BoardingHouseResponseDto {
        val b = boardingHouseRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Boarding house not found") }
        return toDto(b)
    }

    // Owner submits a new listing request (status = PENDING)
    @PostMapping("/request")
    fun submitRequest(
        @Valid @RequestBody dto: BoardingHouseRequestDto,
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

    // Owner edits their own listing.
    // - DECLINED listings return to PENDING (resubmit).
    // - APPROVED listings return to PENDING so an admin re-approves the changes.
    // - PENDING listings cannot be edited until admin acts on them.
    @PutMapping("/{id}")
    fun edit(
        @PathVariable id: Long,
        @Valid @RequestBody dto: BoardingHouseRequestDto,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        val existing = findOwnedListing(id, authentication)

        if (existing.status == BoardingHouseStatus.PENDING) {
            throw IllegalArgumentException(
                "This listing is still pending review; wait for approval or decline before editing"
            )
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

        return toDto(boardingHouseRepository.save(existing.copy(available = dto.available)))
    }

    // Owner permanently deletes one of their own listings
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val existing = findOwnedListing(id, authentication)
        boardingHouseRepository.delete(existing)
        return ResponseEntity.noContent().build()
    }

    // Owner uploads 1..N photos for their own listing.
    // Multipart field name: "files". Max 5 files, 5 MB each, jpeg/png/webp only.
    @PostMapping("/{id}/photos", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadPhotos(
        @PathVariable id: Long,
        @RequestParam("files") files: List<MultipartFile>,
        authentication: Authentication
    ): BoardingHouseResponseDto {
        val existing = findOwnedListing(id, authentication)

        if (files.isEmpty()) {
            throw IllegalArgumentException("No files were uploaded")
        }
        if (existing.photoUrls.size + files.size > MAX_PHOTOS) {
            throw IllegalArgumentException("A listing can have at most $MAX_PHOTOS photos")
        }

        Files.createDirectories(UPLOAD_DIR)

        val newUrls = mutableListOf<String>()
        for (file in files) {
            if (file.isEmpty) {
                throw IllegalArgumentException("One of the uploaded files is empty")
            }
            val contentType = file.contentType
            if (contentType == null || contentType !in ALLOWED_TYPES) {
                throw IllegalArgumentException(
                    "Unsupported file type: $contentType. Allowed: jpeg, png, webp"
                )
            }
            if (file.size > MAX_PHOTO_BYTES) {
                throw IllegalArgumentException("Each photo must be 5 MB or smaller")
            }

            val ext = when (contentType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val filename = "${UUID.randomUUID()}.$ext"
            val target = UPLOAD_DIR.resolve(filename)
            Files.copy(file.inputStream, target, StandardCopyOption.REPLACE_EXISTING)
            newUrls.add("/uploads/$filename")
        }

        val updated = existing.copy(photoUrls = (existing.photoUrls + newUrls).toMutableList())
        return toDto(boardingHouseRepository.save(updated))
    }

    // Search approved, available listings in a town, with optional price range and sorting
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

        return sorted.map { toDto(it) }
    }

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