package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.dto.DistrictDto
import lk.lankaboardinghouse.backend.dto.TownDto
import lk.lankaboardinghouse.backend.repository.DistrictRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/districts")
class DistrictController(private val districtRepository: DistrictRepository) {

    @GetMapping
    fun getAllDistricts(): List<DistrictDto> =
        districtRepository.findAll().map { DistrictDto(it.id, it.name) }
}

@RestController
@RequestMapping("/api/towns")
class TownController(private val townRepository: TownRepository) {

    @GetMapping
    fun getTownsByDistrict(@RequestParam districtId: Long): List<TownDto> =
        townRepository.findByDistrictId(districtId).map {
            TownDto(it.id, it.name, it.district.id, it.district.name)
        }
}