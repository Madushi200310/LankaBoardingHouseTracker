package lk.lankaboardinghouse.backend.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lk.lankaboardinghouse.backend.model.BoardingHouseStatus

data class BoardingHouseRequestDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    val title: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 2000, message = "Description must be at most 2000 characters")
    val description: String,

    @field:NotBlank(message = "Rules and regulations are required")
    @field:Size(max = 2000, message = "Rules must be at most 2000 characters")
    val rulesAndRegulations: String,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: Double,

    @field:NotBlank(message = "Address is required")
    @field:Size(max = 200, message = "Address must be at most 200 characters")
    val addressLine: String,

    @field:NotNull(message = "Town is required")
    val townId: Long,

    @field:NotNull(message = "Owner id is required")
    val ownerId: Long
)

data class DeclineRequestDto(
    val reason: String
)

data class AvailabilityRequestDto(
    val available: Boolean
)

data class BoardingHouseResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val rulesAndRegulations: String,
    val price: Double,
    val addressLine: String,
    val townId: Long,
    val townName: String,
    val districtId: Long,
    val districtName: String,
    val ownerName: String,
    val ownerPhone: String,
    val status: BoardingHouseStatus,
    val declineReason: String?,
    val available: Boolean,
    val photoUrls: List<String> = emptyList()
)