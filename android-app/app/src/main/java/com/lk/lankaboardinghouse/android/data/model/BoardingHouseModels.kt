package com.lk.lankaboardinghouse.android.data.model

data class BoardingHouseRequestDto(
    val title: String,
    val description: String,
    val rulesAndRegulations: String,
    val price: Double,
    val addressLine: String,
    val townId: Long,
    val ownerId: Long
)

data class DeclineRequest(
    val reason: String
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
    val status: String,
    val declineReason: String?
)