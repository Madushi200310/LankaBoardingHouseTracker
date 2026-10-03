package lk.lankaboardinghouse.backend.dto

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