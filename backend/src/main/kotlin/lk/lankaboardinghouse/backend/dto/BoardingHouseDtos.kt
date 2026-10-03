package lk.lankaboardinghouse.backend.dto

import lk.lankaboardinghouse.backend.model.BoardingHouseStatus

data class BoardingHouseRequestDto(
    val title: String,
    val description: String,
    val rulesAndRegulations: String,
    val price: Double,
    val addressLine: String,
    val townId: Long,
    val ownerId: Long
)

data class BoardingHouseResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val rulesAndRegulations: String,
    val price: Double,
    val addressLine: String,
    val townName: String,
    val districtName: String,
    val ownerName: String,
    val ownerPhone: String,
    val status: BoardingHouseStatus
)