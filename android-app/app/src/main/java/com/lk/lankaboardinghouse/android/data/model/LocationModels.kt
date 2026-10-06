package com.lk.lankaboardinghouse.android.data.model

data class DistrictDto(
    val id: Long,
    val name: String
)

data class TownDto(
    val id: Long,
    val name: String,
    val districtId: Long,
    val districtName: String
)