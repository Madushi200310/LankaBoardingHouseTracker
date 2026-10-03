package lk.lankaboardinghouse.backend.repository

import lk.lankaboardinghouse.backend.model.Role
import lk.lankaboardinghouse.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByRole(role: Role): List<User>
}