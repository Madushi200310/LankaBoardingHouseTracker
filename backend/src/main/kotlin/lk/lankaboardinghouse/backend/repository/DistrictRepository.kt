package lk.lankaboardinghouse.backend.repository

import lk.lankaboardinghouse.backend.model.District
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DistrictRepository : JpaRepository<District, Long> {
    fun findByName(name: String): District?
}