package lk.lankaboardinghouse.backend.repository

import lk.lankaboardinghouse.backend.model.Town
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TownRepository : JpaRepository<Town, Long> {
    fun findByDistrictId(districtId: Long): List<Town>
}