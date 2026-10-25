package lk.lankaboardinghouse.backend

import tools.jackson.databind.json.JsonMapper
import lk.lankaboardinghouse.backend.model.District
import lk.lankaboardinghouse.backend.model.Role
import lk.lankaboardinghouse.backend.model.Town
import lk.lankaboardinghouse.backend.model.User
import lk.lankaboardinghouse.backend.repository.BoardingHouseRepository
import lk.lankaboardinghouse.backend.repository.DistrictRepository
import lk.lankaboardinghouse.backend.repository.TownRepository
import lk.lankaboardinghouse.backend.repository.UserRepository
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class BoardingHouseIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: JsonMapper
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var districtRepository: DistrictRepository
    @Autowired lateinit var townRepository: TownRepository
    @Autowired lateinit var boardingHouseRepository: BoardingHouseRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder

    private lateinit var ownerToken: String
    private lateinit var otherOwnerToken: String
    private lateinit var adminToken: String
    private var townId: Long = 0
    private var ownerId: Long = 0

    @BeforeEach
    fun setup() {
        boardingHouseRepository.deleteAll()
        userRepository.deleteAll()

        val district = districtRepository.findAll().firstOrNull()
            ?: districtRepository.save(District(name = "Test District"))
        val town = townRepository.findAll().firstOrNull()
            ?: townRepository.save(Town(name = "Test Town", district = district))
        townId = town.id

        val owner = userRepository.save(
            User(
                fullName = "Owner One",
                email = "owner1@test.lk",
                phoneNumber = "0700000001",
                password = passwordEncoder.encode("Pass@123") ?: "",
                role = Role.OWNER,
                createdAt = java.time.LocalDateTime.now()
            )
        )
        ownerId = owner.id

        val other = userRepository.save(
            User(
                fullName = "Owner Two",
                email = "owner2@test.lk",
                phoneNumber = "0700000002",
                password = passwordEncoder.encode("Pass@123") ?: "",
                role = Role.OWNER,
                createdAt = java.time.LocalDateTime.now()
            )
        )

        val admin = userRepository.save(
            User(
                fullName = "Admin",
                email = "admin@test.lk",
                phoneNumber = "0700000003",
                password = passwordEncoder.encode("Admin@123") ?: "",
                role = Role.ADMIN,
                createdAt = java.time.LocalDateTime.now()
            )
        )

        ownerToken = login(owner.email, "Pass@123")
        otherOwnerToken = login(other.email, "Pass@123")
        adminToken = login(admin.email, "Admin@123")
    }

    private fun login(email: String, password: String): String {
        val body = mapOf("email" to email, "password" to password)
        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readTree(result.response.contentAsString).get("token").asString()
    }

    private fun createListing(token: String, ownerIdValue: Long): Long {
        val body = mapOf(
            "title" to "Nice Room in Town",
            "description" to "A clean room close to the bus stand.",
            "rulesAndRegulations" to "No smoking, no pets.",
            "price" to 15000.0,
            "addressLine" to "123 Main Street",
            "townId" to townId,
            "ownerId" to ownerIdValue
        )
        val result = mockMvc.perform(
            post("/api/boarding-houses/request")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readTree(result.response.contentAsString).get("id").asLong()
    }

    @Test
    fun `owner can upload valid photo`() {
        val id = createListing(ownerToken, ownerId)
        val file = MockMultipartFile("files", "room.jpg", "image/jpeg", ByteArray(1024) { 1 })

        mockMvc.perform(
            multipart("/api/boarding-houses/$id/photos")
                .file(file)
                .header("Authorization", "Bearer $ownerToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.photoUrls").isArray)
            .andExpect(jsonPath("$.photoUrls[0]", startsWith("/uploads/")))
    }

    @Test
    fun `non-owner cannot upload photo`() {
        val id = createListing(ownerToken, ownerId)
        val file = MockMultipartFile("files", "room.jpg", "image/jpeg", ByteArray(1024) { 1 })

        mockMvc.perform(
            multipart("/api/boarding-houses/$id/photos")
                .file(file)
                .header("Authorization", "Bearer $otherOwnerToken")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `invalid file type rejected`() {
        val id = createListing(ownerToken, ownerId)
        val file = MockMultipartFile("files", "notes.txt", "text/plain", "hello".toByteArray())

        mockMvc.perform(
            multipart("/api/boarding-houses/$id/photos")
                .file(file)
                .header("Authorization", "Bearer $ownerToken")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `file too large rejected`() {
        val id = createListing(ownerToken, ownerId)
        val big = ByteArray(6 * 1024 * 1024)
        val file = MockMultipartFile("files", "big.jpg", "image/jpeg", big)

        mockMvc.perform(
            multipart("/api/boarding-houses/$id/photos")
                .file(file)
                .header("Authorization", "Bearer $ownerToken")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `editing approved listing sets status back to PENDING`() {
        val id = createListing(ownerToken, ownerId)

        mockMvc.perform(
            put("/api/boarding-houses/$id/approve")
                .header("Authorization", "Bearer $adminToken")
        ).andExpect(status().isOk).andExpect(jsonPath("$.status").value("APPROVED"))

        val body = mapOf(
            "title" to "Updated Nice Room",
            "description" to "Updated description text.",
            "rulesAndRegulations" to "No smoking.",
            "price" to 18000.0,
            "addressLine" to "123 Main Street",
            "townId" to townId,
            "ownerId" to ownerId
        )
        mockMvc.perform(
            put("/api/boarding-houses/$id")
                .header("Authorization", "Bearer $ownerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `get single listing returns photoUrls`() {
        val id = createListing(ownerToken, ownerId)
        mockMvc.perform(
            get("/api/boarding-houses/$id")
                .header("Authorization", "Bearer $ownerToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.photoUrls").isArray)
    }

    @Test
    fun `invalid price returns 400 with field error`() {
        val body = mapOf(
            "title" to "Short",
            "description" to "desc",
            "rulesAndRegulations" to "rules",
            "price" to 0.0,
            "addressLine" to "addr",
            "townId" to townId,
            "ownerId" to ownerId
        )
        mockMvc.perform(
            post("/api/boarding-houses/request")
                .header("Authorization", "Bearer $ownerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.price").exists())
    }
}