package lk.lankaboardinghouse.backend.controller

import lk.lankaboardinghouse.backend.model.District
import lk.lankaboardinghouse.backend.model.Town
import lk.lankaboardinghouse.backend.repository.DistrictRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/districts")
class DistrictController(private val districtRepository: DistrictRepository) {

    @GetMapping
    fun getAllDistricts(): List<District> = districtRepository.findAll()
}

@RestController
@RequestMapping("/api/towns")
class TownController(private val townRepository: TownRepository) {

    @GetMapping
    fun getTownsByDistrict(@RequestParam districtId: Long): List<Town> =
        townRepository.findByDistrictId(districtId)
}