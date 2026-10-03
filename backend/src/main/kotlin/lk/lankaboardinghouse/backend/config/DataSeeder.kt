package lk.lankaboardinghouse.backend.config

import lk.lankaboardinghouse.backend.model.District
import lk.lankaboardinghouse.backend.model.Role
import lk.lankaboardinghouse.backend.model.Town
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.DistrictRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataSeeder(
    private val districtRepository: DistrictRepository,
    private val townRepository: TownRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String) {
        seedDistrictsAndTowns()
        seedAdminUser()
    }

    private fun seedDistrictsAndTowns() {
        if (districtRepository.count() > 0) {
            println("Districts already seeded, skipping.")
            return
        }

        val data = linkedMapOf(
            "Colombo" to listOf("Colombo", "Dehiwala", "Moratuwa", "Sri Jayawardenepura Kotte", "Kolonnawa"),
            "Gampaha" to listOf("Gampaha", "Negombo", "Ja-Ela", "Wattala", "Kelaniya"),
            "Kalutara" to listOf("Kalutara", "Panadura", "Horana", "Beruwala"),
            "Kandy" to listOf("Kandy", "Peradeniya", "Gampola", "Katugastota"),
            "Matale" to listOf("Matale", "Dambulla", "Galewela"),
            "Nuwara Eliya" to listOf("Nuwara Eliya", "Hatton", "Talawakele", "Ginigathena"),
            "Galle" to listOf("Galle", "Hikkaduwa", "Ambalangoda", "Baddegama"),
            "Matara" to listOf("Matara", "Weligama", "Akuressa"),
            "Hambantota" to listOf("Hambantota", "Tangalle", "Tissamaharama"),
            "Jaffna" to listOf("Jaffna", "Nallur", "Chavakachcheri"),
            "Kilinochchi" to listOf("Kilinochchi", "Paranthan"),
            "Mannar" to listOf("Mannar", "Nanattan"),
            "Vavuniya" to listOf("Vavuniya", "Cheddikulam"),
            "Mullaitivu" to listOf("Mullaitivu", "Puthukkudiyiruppu"),
            "Trincomalee" to listOf("Trincomalee", "Kinniya", "Kantale"),
            "Batticaloa" to listOf("Batticaloa", "Kattankudy", "Eravur"),
            "Ampara" to listOf("Ampara", "Kalmunai", "Akkaraipattu"),
            "Kurunegala" to listOf("Kurunegala", "Kuliyapitiya", "Narammala"),
            "Puttalam" to listOf("Puttalam", "Chilaw", "Wennappuwa"),
            "Anuradhapura" to listOf("Anuradhapura", "Kekirawa", "Medawachchiya"),
            "Polonnaruwa" to listOf("Polonnaruwa", "Hingurakgoda", "Medirigiriya"),
            "Badulla" to listOf("Badulla", "Bandarawela", "Haputale"),
            "Monaragala" to listOf("Monaragala", "Wellawaya", "Bibile"),
            "Ratnapura" to listOf("Ratnapura", "Embilipitiya", "Balangoda"),
            "Kegalle" to listOf("Kegalle", "Mawanella", "Warakapola")
        )

        data.forEach { (districtName, towns) ->
            val district = districtRepository.save(District(name = districtName))
            towns.forEach { townName ->
                townRepository.save(Town(name = townName, district = district))
            }
        }

        println("Seeded ${data.size} districts and ${data.values.sumOf { it.size }} towns.")
    }

    private fun seedAdminUser() {
        if (userRepository.findByEmail("admin@lankaboarding.lk") != null) {
            println("Admin user already exists, skipping.")
            return
        }

        val admin = User(
            fullName = "System Admin",
            email = "admin@lankaboarding.lk",
            password = passwordEncoder.encode("Admin@123") ?: error("Password encoding failed"),
            phoneNumber = "0770000000",
            role = Role.ADMIN
        )

        userRepository.save(admin)
        println("Seeded default admin user (email: admin@lankaboarding.lk, password: Admin@123)")
    }
}